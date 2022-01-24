package com.ipssi.rfid.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.db.RFIDMasterDao;

public class TestAutoSuggest extends JPanel{
	private static final long serialVersionUID = 1L;
	private JTextField tf;
	private JComboBox combo = new JComboBox();
	private DefaultComboBoxModel model = null;


	public TestAutoSuggest(final ArrayList<ComboItem> source) {
		super(new BorderLayout());
		combo.setEditable(true);
		tf = (JTextField) combo.getEditor().getEditorComponent();
		tf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				String text = tf.getText();
				int code = e.getKeyCode();
				if(code==KeyEvent.VK_ENTER) {
					hide_flag = true;
					combo.hidePopup();
				}else if(code==KeyEvent.VK_ESCAPE) {
					hide_flag = true; 
				}else if(code==KeyEvent.VK_RIGHT) {
					
				}else if(text.length() >= 3) {
					clearComboBox();
					setSourceList(text);
					tf.setText(text);
					combo.showPopup();
				}else {
					//clearComboBox();
					combo.hidePopup();
				}
			}
			private void clearComboBox(){
				combo.removeAllItems();
			}
			private void setSourceList(String text) {
				try{
					for(int i=0,is=source == null ? 0 : source.size();i<is;i++){
						ComboItem item = source.get(i);
						if(item != null && item.getLabel() != null && text != null && item.getLabel().toUpperCase().contains(text.toUpperCase()))
							combo.addItem(item);
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			/*public void keyPressed(KeyEvent e) {
				String text = tf.getText();
				int code = e.getKeyCode();
				if(code==KeyEvent.VK_ENTER) {
					hide_flag = true; 
				}else if(code==KeyEvent.VK_ESCAPE) {
					hide_flag = true; 
				}else if(code==KeyEvent.VK_RIGHT) {
					
				}
			 }*/
		});
		/*String[] countries = {"Afghanistan", "Albania", "Algeria", "Andorra", "Angola","Argentina"
				,"Armenia","Austria","Bahamas","Bahrain", "Bangladesh","Barbados", "Belarus","Belgium",
				"Benin","Bhutan","Bolivia","Bosnia & Herzegovina","Botswana","Brazil","Bulgaria",
				"Burkina Faso","Burma","Burundi","Cambodia","Cameroon","Canada", "China","Colombia",
				"Comoros","Congo","Croatia","Cuba","Cyprus","Czech Republic","Denmark", "Georgia",
				"Germany","Ghana","Great Britain","Greece","Hungary","Holland","India","Iran","Iraq",
				"Italy","Somalia", "Spain", "Sri Lanka", "Sudan","Suriname", "Swaziland","Sweden",
				"Switzerland", "Syria","Uganda","Ukraine","United Arab Emirates","United Kingdom",
				"United States","Uruguay","Uzbekistan","Vanuatu","Venezuela","Vietnam",
				"Yemen","Zaire","Zambia","Zimbabwe"};
		for(int i=0;i<countries.length;i++){
			v.addElement(countries[i]);
		}*/
		//setModel(new DefaultComboBoxModel(v), "");
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("AutoSuggestion Box"));
		p.add(combo, BorderLayout.NORTH);
		add(p);
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		setPreferredSize(new Dimension(300, 150));
	}
	private boolean hide_flag = false;
	
	private void setModel(DefaultComboBoxModel mdl, String str) {
		combo.setModel(mdl);
		combo.setSelectedIndex(-1);
		tf.setText(str);
	}
	private static DefaultComboBoxModel getSuggestedModel(java.util.List<String> list, String text) {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for(String s: list) {
			if(s.toUpperCase().startsWith(text.toUpperCase()))
				m.addElement(s);
		}
		return m;
	}
	public static void main(String[] args) {
		ArrayList<ComboItem> source = null;
		Connection conn = null;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			Vehicle veh = new Vehicle();
			veh.setStatus(1);
			ArrayList<Vehicle> list = (ArrayList<Vehicle>) RFIDMasterDao.getList(conn, veh, null);
			for(int i=0,is=list == null ? 0 : list.size();i < is;i++){
				if(source == null)
					source = new ArrayList<ComboItem>();
				source.add(new ComboItem(list.get(i).getId(), list.get(i).getStdName()));
			}
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new TestAutoSuggest(source));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}