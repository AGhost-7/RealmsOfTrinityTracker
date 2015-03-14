package aghost7.realmsoftrinitytracker

import java.awt.{CheckboxMenuItem => Checkbox, MenuItem, PopupMenu, TrayIcon}
import java.awt.event.ItemEvent
import javax.swing._

import java.awt.SystemTray
import aghost7.bebop.event.implicits._

import globals._

object ControlBar {
	
	var notifyEnabled = true
	lazy val tray = SystemTray.getSystemTray
	
	lazy val notif = new Checkbox("Notify", true)
	lazy val close = new MenuItem("Close") 
	
	lazy val popup = new PopupMenu() {
		add(notif)
		addSeparator()
		add(close)
	}
	
	lazy val img = 
		new ImageIcon(getClass.getResource("/icon.png")).getImage
	lazy val trayIcon = new TrayIcon(img, appName, popup)
	
	if(SystemTray.isSupported()){
		
		notif.onItemStateChanged { ev => 
			notifyEnabled = ev.getStateChange() == ItemEvent.SELECTED
		}
		
		close.onActionPerformed { _ => 
			Tracker.thread.interrupt()
			System.exit(0)
		}
		
		tray.add(trayIcon)
	} else {
		writer { put =>
			put("---System tray control not supported---")
		}
		System.exit(1)
	}
	
	def notify(title: String, message: String): Unit = {
		SwingUtilities.invokeLater(new Runnable {
			def run = 
				if(notifyEnabled){
					trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
				}
		})
	}
}
