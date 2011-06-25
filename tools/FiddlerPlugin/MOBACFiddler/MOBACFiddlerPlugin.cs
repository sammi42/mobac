using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.Windows.Forms;
using Fiddler;

// Extension requires Fiddler 2.2.8.6+ because it uses types introduced in v2.2.8...
[assembly: Fiddler.RequiredVersion("2.2.8.6")]

namespace MOBACFiddler
{
    public class MOBACExtension : IAutoTamper
    {

        private List<String> urlList = new List<String>();

        private static int LINE_HEIGHT = 18;
        private Font font = new Font("Arial", 10f, FontStyle.Bold);

        private Boolean urlPainterEnabled = false;
        private MenuItem miToggleUrlPainter = null;

        // Called when Fiddler User Interface is fully available
        public void OnLoad()
        {
            Debug.Write("Loading MOBAC Fiddler plugin");
            /*TabPage oPage = new TabPage("MOBAC");
            oPage.ImageIndex = (int)Fiddler.SessionIcons.Timeline;
            oView = new TimelineView();
            oPage.Controls.Add(oView);
            oView.Dock = DockStyle.Fill;
            FiddlerApplication.UI.tabsViews.TabPages.Add(oPage);*/
            // Called when Fiddler loads this extension.
            MenuItem mnuMOBAC = new MenuItem("&MOBAC Extension");
            MenuItem miClear = new MenuItem("&Clear Cache");
            miToggleUrlPainter = new MenuItem("&Enable/Disable URL tile painter");
            miToggleUrlPainter.Checked = urlPainterEnabled;

            mnuMOBAC.MenuItems.Add(miClear);
            mnuMOBAC.MenuItems.Add(miToggleUrlPainter);

            miClear.Click += new EventHandler(miClear_Click);
            miToggleUrlPainter.Click += new EventHandler(miToggleUrlPainter_Click);
            FiddlerApplication.UI.Menu.MenuItems.Add(mnuMOBAC);

        }

        // Called when Fiddler is shutting down
        public void OnBeforeUnload()
        {
        }

        // Called before the user can edit a request using the Fiddler Inspectors
        public void AutoTamperRequestBefore(Session oSession)
        {
        }

        // Called after the user has had the chance to edit the request using the Fiddler Inspectors, but before the request is sent
        public void AutoTamperRequestAfter(Session oSession)
        {
        }

        // Called before the user can edit a response using the Fiddler Inspectors, unless streaming.
        public void AutoTamperResponseBefore(Session oSession)
        {
            if (!urlPainterEnabled)
                return;
            oSession.utilDecodeResponse();
            if (oSession.responseCode != 200)
                return;
            String mimeType = oSession.oResponse.MIMEType;
            if (!mimeType.StartsWith("image/"))
                return;
            byte[] body = oSession.responseBodyBytes;
            Image image = Image.FromStream(new MemoryStream(body));
            if ((image.Width != 256) || (image.Height != 256))
                return;
            urlList.Add(oSession.url);
            Image newImage = new Bitmap(image);
            Graphics g = Graphics.FromImage(newImage);
            Brush blackBrush = new SolidBrush(Color.Black);
            Brush whiteBrush = new SolidBrush(Color.White);
            String url = oSession.url;
            int ind = url.IndexOf(oSession.hostname);
            if (ind >= 0)
                url = url.Substring(ind + oSession.hostname.Length);
            Brush bgBrush = new SolidBrush(Color.FromArgb(150, 100, 100, 100));
            g.FillRectangle(bgBrush, 0, 0, 255, LINE_HEIGHT);
            g.DrawString(oSession.hostname, font, whiteBrush, 1f, 1f);
            SizeF urlSize = g.MeasureString(url, font);
            if (urlSize.Width > 254)
            {
                int lines = (int)Math.Ceiling(urlSize.Width / 255);
                int chars = url.Length / lines;
                int paintYCoord = LINE_HEIGHT;
                while (url.Length > 0)
                {
                    int lineLength = 0;
                    String lineStr;
                    do
                    {
                        lineStr = url.Substring(0, lineLength++);
                        urlSize = g.MeasureString(lineStr, font);
                    } while (urlSize.Width < 250 && lineLength < url.Length);

                    url = url.Substring(lineLength);
                    g.FillRectangle(bgBrush, 0, paintYCoord, 255, LINE_HEIGHT);
                    g.DrawString(lineStr, font, whiteBrush, 1f, paintYCoord);
                    paintYCoord += LINE_HEIGHT;
                }
            }
            else
            {
                g.FillRectangle(bgBrush, LINE_HEIGHT, 0, 255, 30);
                g.DrawString(url, font, whiteBrush, 1f, 15f);
            }
            g.DrawRectangle(new Pen(blackBrush), 0, 0, 255, 255);

            MemoryStream outStream = new MemoryStream(30000);
            ImageFormat outputFormat = null;
            Debug.Write("Mime type: " + mimeType + " " + oSession.url);
            if ("image/jpeg".Equals(mimeType))
                outputFormat = ImageFormat.Jpeg;
            else if ("image/gif".Equals(mimeType))
                outputFormat = ImageFormat.Gif;
            if (outputFormat == null)
            {
                outputFormat = ImageFormat.Png;
                oSession.oResponse.headers.Remove("Content-Type");
                oSession.oResponse.headers.Add("Content-Type", "image/png");
            }

            newImage.Save(outStream, outputFormat);

            body = outStream.ToArray();
            oSession.oResponse.headers.Remove("Cache-Control");
            oSession.oResponse.headers.Remove("Age");
            oSession.oResponse.headers.Remove("ETag");
            oSession.oResponse.headers.Remove("Date");
            oSession.oResponse.headers.Remove("Content-Length");
            oSession.oResponse.headers.Add("Cache-Control", "no-cache");
            oSession.oResponse.headers.Add("Content-Length", body.Length.ToString());
            oSession.responseBodyBytes = body;
        }

        // Called after the user edited a response using the Fiddler Inspectors.  Not called when streaming.
        public void AutoTamperResponseAfter(Session oSession)
        {
        }

        // Called Fiddler returns a self-generated HTTP error (for instance DNS lookup failed, etc)
        public void OnBeforeReturningError(Session oSession)
        {
        }

        #region UI Code
        void miClear_Click(object sender, EventArgs e)
        {
            urlList.Clear();
        }

        void miToggleUrlPainter_Click(object sender, EventArgs e)
        {
            this.urlPainterEnabled = !this.urlPainterEnabled;
            if (miToggleUrlPainter != null)
                miToggleUrlPainter.Checked = urlPainterEnabled;
        }
        #endregion
    }
}
