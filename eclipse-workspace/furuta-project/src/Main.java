import javax.swing.*;

public class Main {
	public static void main(String[] argv) {
		Regul regul = new Regul();
		final OpCom opcom = new OpCom(); //Must be declared final since it is used in an inner class
		
		regul.setOpCom(opcom);
		opcom.setRegul(regul);
		
		Runnable initializeGUI = new Runnable() {
		    public void run() {
		      opcom.initializeGUI();
		      opcom.start();
		      I_ODemo demo = new I_ODemo();
		      demo.start();
		    }
		};
		try {
			SwingUtilities.invokeAndWait(initializeGUI);
		} catch (Exception x) {
			x.printStackTrace();
		  return;
		}
		regul.start();
	}
}