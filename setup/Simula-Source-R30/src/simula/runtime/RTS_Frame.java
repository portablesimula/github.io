/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JFrame;

/// RTS Frame used by [RTS_Drawing] and [RTS_ConsolePanel]
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_Frame.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
@SuppressWarnings("serial")
public class RTS_Frame extends JFrame {
	
	/// The opened frames.
	private static Vector<JFrame> openFrames;
	
	/// Create a new RTS_Frame
	public RTS_Frame() {
		addWindowListener(this);
		if(openFrames == null) openFrames = new Vector<JFrame>();
		openFrames.add(this);
	}

	/// Close this RTS_Frame.
	private void close() {
		openFrames.remove(this);
		if(openFrames.size() == 0) {
			System.exit(0);
//			RTS_ENVIRONMENT.exit(0);
		}
	}
	
	/// Add WindowListener to a RTS_Frame.
	/// @param frame a Frame.
	private static void addWindowListener(RTS_Frame frame) {
		frame.addWindowListener(new WindowListener() {
			@Override public void windowOpened(WindowEvent e) {}
			@Override public void windowClosed(WindowEvent e) {}
			@Override public void windowIconified(WindowEvent e) {}
			@Override public void windowDeiconified(WindowEvent e) {}
			@Override public void windowActivated(WindowEvent e) {}
			@Override public void windowDeactivated(WindowEvent e) {}

			@Override
			public void windowClosing(WindowEvent e) {
				if (e.getID() == WindowEvent.WINDOW_CLOSING) {
					frame.close();
				}
			}
		});
	}

}
