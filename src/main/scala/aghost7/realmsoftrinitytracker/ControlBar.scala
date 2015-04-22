package aghost7.realmsoftrinitytracker

import java.awt.{CheckboxMenuItem => Checkbox, MenuItem, PopupMenu, TrayIcon}
import java.awt.event.ItemEvent
import javax.swing._

import java.awt.SystemTray
import aghost7.bebop.event.implicits._
import sys.process._
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

	private lazy val _notify: (String, String) => Unit = {
		if(System.getProperty("os.name") == "Linux"
				&& "which notify-send".!! != ""){
			(title, body) =>
				Seq("notify-send", title, body) !
		} else if(SystemTray.isSupported()) {
			(title, body) =>
				trayIcon.displayMessage(title, body, TrayIcon.MessageType.INFO)
		} else {
			(title, body) =>
				Unit
		}
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
	}

	def notify(title: String, message: String): Unit = {
		SwingUtilities.invokeLater(new Runnable {
			def run = 
				if(notifyEnabled){
					_notify(title, message)
				}
		})
	}
}
