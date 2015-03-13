package aghost7.realmsoftrinitytracker

import java.awt.event._
import java.awt.{CheckboxMenuItem => Checkbox, MenuItem, PopupMenu, TrayIcon}

import javax.swing._
import java.awt.SystemTray

import aghost7.scriptly._

import globals._

object ControlBar extends ItemListener with ActionListener {
	val writer = Writer("exceptions.txt") _
	var notifyEnabled = true
	lazy val tray = SystemTray.getSystemTray
	
	lazy val notif = new Checkbox("Notify", true)
	lazy val close = new MenuItem("Close")
	lazy val popup = new PopupMenu()
	lazy val trayIcon = new TrayIcon(null, appName, popup)
	
	if(SystemTray.isSupported()){
		
		notif.addItemListener(this)
		close.addActionListener(this)
		
		popup.add(notif)
		popup.addSeparator()
		popup.add(close)
		
		tray.add(trayIcon)
	} else {
		writer { put =>
			put("---System tray control not supported---")
		}
		System.exit(1)
	}
	
	def actionPerformed(e : ActionEvent): Unit = {
		System.exit(0)
	}
	
	def itemStateChanged(ev: ItemEvent): Unit = {
		notifyEnabled = ev.getStateChange() == ItemEvent.SELECTED
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