package aghost7.realmsoftrinitytracker

import javax.swing.{
	SwingUtilities,
	JPanel,
	JLabel,
	JFrame,
	ImageIcon
}

class Loadscreen extends JFrame {
		
	setUndecorated(true)
	val pMain = getContentPane.asInstanceOf[JPanel]
	val img = new ImageIcon(getClass.getResource("/loadscreen.png"))
	pMain.setOpaque(true)
	
	pMain.add(new JLabel(img))
	setOpacity(0.5f)
	pack()
	setLocationRelativeTo(null)
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	setVisible(true)
	
	def animate(callback: => Unit): Unit = {
		import aghost7.bebop.event.BConcurrent._
		startThread {
			for(f <- 0.5f until 1.0f by 0.05f){
				Thread.sleep(40)
				setOpacity(f)
			}
			Thread.sleep(1000)
			for(f <- 0.95f until 0.0f by -0.05f){
				Thread.sleep(40)
				setOpacity(f)
			}
			dispose()
			setVisible(false)
			invokeLater(callback)
		}
	}
}

object Loadscreen {
	val s = 'foo
	def animate(callback: => Unit): Unit = {
		val ld = new Loadscreen
		ld.animate(callback)
	}
}