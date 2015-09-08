package com.example.hotelproject;

import java.util.ArrayList;

import multipayerpaymentscreens.TipCouponMultipayer;
import payments.PaymentSettings;
import serverutil.Fraction;
import serverutil.HandleServerDataTable;
import serverutil.ItemDescDS;
import serverutil.Order;
import databaseutil.DBHelperWhoHadTheLobster;
import databaseutil.DBHelperFraction;
import databaseutil.FractionRowDS;
import databaseutil.PersonRowDS;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class WhoHadTheLobster extends ActionBarActivity {
	
	ListView lv_item_table;
	ArrayList<ItemDescDS> all_items;
	HandleServerDataTable handleServerDataTable;
	DBHelperWhoHadTheLobster dBHelperWhoHadTheLobster;
	DBHelperFraction dBHelperFraction;
	Button bt_total_amount;
	TableLayout table_layout;
	ArrayList<TableRow> tableRows;
	
	boolean reviewPage;
	
	boolean payers_paid[];
	String[] payers_name;
	boolean payers_hilight[];
	double[] payers_payment_due;
	boolean[] payers_delete_bool;
	
	ArrayList<Order> payers_order;
	
	int tableno;
	int order_id;
	int no_of_payers;
	double perpersonamount,totaldue;
	int total , failure = 0;
	
	double drop_item_amount;
	int drop_item_remove_index;
	
	MyListAdapter adapter ;
	ListView billListView ;
	
	RelativeLayout rl_tempFoodRow;
	Drawable rlBackground;
	
	int payer_remove_index = -1;
	boolean is_paid_page = false;
	
	private void loadInitialData(){
		
		//load from prev activity
		   SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences",Context.MODE_PRIVATE);
			order_id=sharedPreferences.getInt("order_id",0);                       
			totaldue=Double.parseDouble(sharedPreferences.getString("totaldue", "0")) ;
			no_of_payers=sharedPreferences.getInt("no_of_payers", 0);             
			 tableno = sharedPreferences.getInt("table_no", 0);
// 	         System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> no_of_payers " + tableno);
 	         
		     lv_item_table=(ListView)findViewById(R.id.lv_item_table_who_had_the_lobster);
		  //   bt_total_amount=(Button)findViewById(R.id.bt_amount_who_had_the_lobster);
		     
		     dBHelperWhoHadTheLobster=new DBHelperWhoHadTheLobster(this);
		     dBHelperFraction = new DBHelperFraction(this);
		     handleServerDataTable=new HandleServerDataTable(tableno);
			 handleServerDataTable.fetchDataFromServer();
			 while(handleServerDataTable.parsingComplete);	
			 all_items=handleServerDataTable.getTableItems();
			 
			// bt_total_amount.setText("Total: "+PaymentSettings.CURRENCY_SIGN+totaldue);
			 tableRows=new ArrayList<TableRow>();
	}
	
	MyView myView;
	View tempView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extraBundle;
		extraBundle=getIntent().getBundleExtra("databundle");
		reviewPage=extraBundle.getBoolean("review");
		
		setContentView(R.layout.activity_who_had_the_lobster);
		myView =new MyView(this);
		loadInitialData();
		if(tableno==0){
			Toast.makeText(this,"Something Went Wrong Server is Not Responding or Table Does not Exits",Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
			System.out.println("==========++++++++1");
		}
		else if(all_items==null)
		{
			Toast.makeText(this,"Bill For Table No "+tableno+ " has already paid",Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
			System.out.println("==========++++++++2");
		
		}
	   else if(!dBHelperWhoHadTheLobster.isAnyOnePaid(order_id))
	    {
		    ArrayList<ItemDescDS> n_all_items=new ArrayList<ItemDescDS>();
	    	ArrayList<FractionRowDS> fraction_list = dBHelperFraction.getAllPersons(order_id);
			for (int k=0; k<fraction_list.size(); k++) {
				FractionRowDS fractionItem = fraction_list.get(k);
			    if (fractionItem.person_name.equals("empty")) {
				  ItemDescDS descObj = new ItemDescDS();
				  descObj.item_name = fractionItem.item_name;
				  descObj.price_per_unit = fractionItem.price;
				  descObj.units = new Fraction(fractionItem.units, fractionItem.f_up, fractionItem.f_down);
				  descObj.total_price=descObj.units.value()*descObj.price_per_unit;
				  descObj.total_price=(double) Math.round(descObj.total_price * 100) / 100;
				  n_all_items.add(descObj);
			    }
		    }
			
			if (n_all_items.size() > 0 && reviewPage) {
				adapter = new MyListAdapter(WhoHadTheLobster.this, R.layout.food_items_row, n_all_items);
		    	billListView = (ListView)findViewById(R.id.lv_item_table_who_had_the_lobster);
			    billListView.setAdapter(adapter);	
		    	loadData();
			    prepareScreen();
			} else {
				dBHelperFraction.isAnyOnePaid(order_id);
			    order_id=handleServerDataTable.getOrderId();
			    
			    adapter = new MyListAdapter(WhoHadTheLobster.this, R.layout.food_items_row, all_items);
			    billListView = (ListView)findViewById(R.id.lv_item_table_who_had_the_lobster);
			    billListView.setAdapter(adapter);		    
			    loadData();
			    prepareScreen();
			}
		    
		    System.out.println("==========++++++++3");
	    }else{
	    	all_items=new ArrayList<ItemDescDS>();
	    	
	    	ArrayList<FractionRowDS> fraction_list = dBHelperFraction.getAllPersons(order_id);
			for (int k=0; k<fraction_list.size(); k++) {
				FractionRowDS fractionItem = fraction_list.get(k);
			    if (fractionItem.person_name.equals("empty")) {
				  ItemDescDS descObj = new ItemDescDS();
				  descObj.item_name = fractionItem.item_name;
				  descObj.price_per_unit = fractionItem.price;
				  descObj.units = new Fraction(fractionItem.units, fractionItem.f_up, fractionItem.f_down);
				  descObj.total_price=descObj.units.value()*descObj.price_per_unit;
				  descObj.total_price=(double) Math.round(descObj.total_price * 100) / 100;
				  all_items.add(descObj);
			    }
		    }   
			   
	    	adapter = new MyListAdapter(WhoHadTheLobster.this, R.layout.food_items_row, all_items);
	    	billListView = (ListView)findViewById(R.id.lv_item_table_who_had_the_lobster);
		    billListView.setAdapter(adapter);	
	    	loadData();
		    prepareScreen();
		    
		    System.out.println("==========++++++++4");
	    }
		
		
	}

	public void loadData(){
		payers_paid = new boolean[no_of_payers];
		   if (dBHelperWhoHadTheLobster.isThereAnyRecord(order_id)) {
			   if(!dBHelperWhoHadTheLobster.isAnyOnePaid(order_id)){
				   if (!reviewPage) {
					   dBHelperWhoHadTheLobster.deleteData(order_id);
					   dBHelperFraction.deleteData(order_id);
					   payers_paid=new boolean[no_of_payers];
			    		payers_name=new String[no_of_payers];
			    		payers_payment_due=new double[no_of_payers];
			    		payers_delete_bool = new boolean[no_of_payers];
			    		payers_hilight=new boolean[no_of_payers];
		    			for(int i=0;i<no_of_payers;i++){
			    			  payers_paid[i]=false;
			    			  payers_name[i]="Payer "+(i+1);
			    			  payers_payment_due[i]=0;
			    			  payers_hilight[i]=false;
			    			  dBHelperWhoHadTheLobster.insertData(order_id, payers_name[i],false, payers_payment_due[i]);
			    		}
				   } else {
					   no_of_payers=dBHelperWhoHadTheLobster.getNoPayers(order_id);
						payers_paid=new boolean[no_of_payers];
						payers_name=new String[no_of_payers];
						payers_payment_due=new double[no_of_payers];
						payers_delete_bool = new boolean[no_of_payers];
						payers_hilight=new boolean[no_of_payers];
			    		
			    		ArrayList<PersonRowDS> array_list=dBHelperWhoHadTheLobster.getAllPersons(order_id);
						  for(int i=0;i<no_of_payers;i++){
							  payers_paid[i]=array_list.get(i).PAID;
							  payers_name[i]=array_list.get(i).PERSON_NAME;
							  payers_payment_due[i]=array_list.get(i).PAID_AMOUNT;
							  payers_hilight[i]=false;
						  }
				   }
			   }else{
					no_of_payers=dBHelperWhoHadTheLobster.getNoPayers(order_id);
					payers_paid=new boolean[no_of_payers];
					payers_name=new String[no_of_payers];
					payers_payment_due=new double[no_of_payers];
					payers_delete_bool = new boolean[no_of_payers];
					payers_hilight=new boolean[no_of_payers];
		    		
		    		ArrayList<PersonRowDS> array_list=dBHelperWhoHadTheLobster.getAllPersons(order_id);
					  for(int i=0;i<no_of_payers;i++){
						  payers_paid[i]=array_list.get(i).PAID;
						  payers_name[i]=array_list.get(i).PERSON_NAME;
						  payers_payment_due[i]=array_list.get(i).PAID_AMOUNT;
						  payers_hilight[i]=false;
					  }
					  
			   }
		   }else{
		    	payers_paid=new boolean[no_of_payers];
		    	payers_name=new String[no_of_payers];
		    	payers_payment_due=new double[no_of_payers];
		    	payers_delete_bool = new boolean[no_of_payers];
		    	payers_hilight=new boolean[no_of_payers];
			    for(int i=0;i<no_of_payers;i++) {
	    			  payers_paid[i]=false;
	    			  payers_name[i]="Payer "+(i+1);
	    			  payers_payment_due[i]=0;
	    			  payers_hilight[i]=false;
	    			  dBHelperWhoHadTheLobster.insertData(order_id, payers_name[i],false, payers_payment_due[i]);
	    		}
			    
		   }
		   
		   payers_order = new ArrayList<Order>();
		   for(int i=0;i<no_of_payers;i++){
			   Order orderItem = new Order();
			   orderItem.payer_name = payers_name[i]; 
			   orderItem.all_items = new ArrayList<ItemDescDS>();
			   payers_order.add(orderItem);
		   }
		  
		   if (dBHelperWhoHadTheLobster.isThereAnyRecord(order_id)) {
//			   if(dBHelperWhoHadTheLobster.isAnyOnePaid(order_id)){
				   
				   ArrayList<FractionRowDS> fraction_list = dBHelperFraction.getAllPersons(order_id);
				   for (int j=0; j<no_of_payers; j++) { 
					  for (int k=0; k<fraction_list.size(); k++) {
						  FractionRowDS fractionItem = fraction_list.get(k);
						  if (payers_order.get(j).payer_name.equals(fractionItem.person_name)) {
							  ItemDescDS descObj = new ItemDescDS();
							  descObj.item_name = fractionItem.item_name;
							  descObj.price_per_unit = fractionItem.price;
							  descObj.units = new Fraction(fractionItem.units, fractionItem.f_up, fractionItem.f_down);
							  payers_order.get(j).all_items.add(descObj);
						  }
					  }
				
				   }
//			   }
		   }else{ 
			   
		   }
	}
	
	@SuppressLint("InflateParams")
	public void prepareScreen() {
		table_layout=(TableLayout)findViewById(R.id.tl_who_had_the_lobster);
		table_layout.removeAllViews();
		Button tempb = null;
		TextView temptv=null;

		for(int i=0;i<no_of_payers;i++ ) {
			
			final TableRow tr=(TableRow)LayoutInflater.from(WhoHadTheLobster.this).inflate(R.layout.payer_row_who_had_the_lobster, null);
			tempb = (Button)tr.findViewById(R.id.bt_payer_name_who_had_the_lobster);
			setUpButton(tempb,payers_name[i]);			
			temptv=(TextView)tr.findViewById(R.id.tv_payer_amount_who_had_the_lobster);
			temptv.setText(PaymentSettings.CURRENCY_SIGN+payers_payment_due[i]);

			if(payers_paid[i]==true) {
				tempb.setEnabled(false);
				tempb.setBackgroundResource(R.drawable.button_flat_red_normal);
			}
			
			tableRows.add(tr);
			table_layout.addView(tr);
		}
		
	}
	
	
private void setUpButton(final Button tempb,final String payer_name) {
		
		tempb.setText(payer_name);
		tempb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.v("My Log" ,"Drop right XianA!!!!!!!!!");
				
				if(billListView.getAdapter().getCount()==0){
					TextView temptv=null;
				    for(int i=0;i<tableRows.size();i++){
				    	
				    	Button tempb=(Button)tableRows.get(i).findViewById(R.id.bt_payer_name_who_had_the_lobster);
				    	if(tempb==((Button)v))
				    	{
				    		temptv=(TextView)tableRows.get(i).findViewById(R.id.tv_payer_amount_who_had_the_lobster);
				    	    break;
				    	}
				
				    }
				    
				    if(!dBHelperWhoHadTheLobster.isAnyOnePaid(order_id)){
				    	
				    	dBHelperWhoHadTheLobster.deleteData(order_id);
				    	for(int i=0;i<no_of_payers;i++){
				    		if (!payers_delete_bool[i])
				    			dBHelperWhoHadTheLobster.insertData(order_id, payers_name[i],false,payers_payment_due[i]);
				    	}
				    	
				    	dBHelperFraction.deleteData(order_id);
				    	for(int k=0;k<no_of_payers;k++){
				    		if (!payers_delete_bool[k])
				    			for (int j=0; j<payers_order.get(k).all_items.size(); j++) {
				    				ItemDescDS itemObj = payers_order.get(k).all_items.get(j);
				    				dBHelperFraction.insertData(order_id, payers_name[k], itemObj.item_name, itemObj.price_per_unit, itemObj.units.integer, itemObj.units.numerator, itemObj.units.denominator);
				    			}
				    	}
				    	
				    	int jj = dBHelperFraction.getAllPersons(order_id).size();
				    	ArrayList<FractionRowDS> fraction_list = dBHelperFraction.getAllPersons(order_id);
				    	System.out.println(jj+"-1111111111");
				    } else {
				    	dBHelperWhoHadTheLobster.deleteDataNoPaid(order_id);
				    	for(int i=0;i<no_of_payers;i++){
				    		if (!payers_delete_bool[i] && !payers_paid[i])
				    			dBHelperWhoHadTheLobster.insertData(order_id, payers_name[i],false,payers_payment_due[i]);
				    	}
				    	
				    	dBHelperFraction.deleteData(order_id);
				    	for(int k=0;k<no_of_payers;k++){
				    		if (!payers_delete_bool[k])
				    			for (int j=0; j<payers_order.get(k).all_items.size(); j++) {
				    				ItemDescDS itemObj = payers_order.get(k).all_items.get(j);
				    				dBHelperFraction.insertData(order_id, payers_name[k], itemObj.item_name, itemObj.price_per_unit, itemObj.units.integer, itemObj.units.numerator, itemObj.units.denominator);
				    			}
				    	}
				    }
				    	
					Bundle extradataBundle=new Bundle();
					extradataBundle.putInt("order_id", order_id);
					extradataBundle.putString("payer_name",((Button)v).getText().toString());
					extradataBundle.putDouble("totaldue", Double.parseDouble(temptv.getText().toString().replace("$","")) );
					extradataBundle.putString("who_is_paying","who_had_the_lobster");
					
					Bundle bundle=new Bundle();
					bundle.putBundle("databundle",extradataBundle);
			        Intent sd=new Intent(WhoHadTheLobster.this,TipCouponMultipayer.class);
			        sd.putExtras(bundle);
			        startActivity(sd);
				}else{
					 for(int i=0;i<no_of_payers;i++){
						 if(tempb.getText().toString().equals(payers_name[i])){
							 if(payers_hilight[i]==false){
								 tempb.setBackgroundResource(R.drawable.button_flat_disable_grey);
								 payers_hilight[i]=true;
							 }else{
								 tempb.setBackgroundResource(R.drawable.button_flat_green_purple);
								 payers_hilight[i]=false;
							 }
						 }
					 }
				}
			}
		});
		
		tempb.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				ClipData data = ClipData.newPlainText("DRAG", "");
		        View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
	 	        v.startDrag(data, shadow, null, 0);
	 	        
	 	       Log.v("My Log" ,"Long Touch XianA!!!!!!!!!");
		        
		        for(int i=0;i<tableRows.size();i++){
		        	Button bt_temp=(Button)tableRows.get(i).findViewById(R.id.bt_payer_name_who_had_the_lobster);
		        	if(tempb.equals(bt_temp)){
		        		payer_remove_index=i;
		        		return false;
		        	}
		        }
		        
        		payer_remove_index=-1;
				return false;
			}
		});

		tempb.setOnDragListener(new View.OnDragListener() {
		    @SuppressLint("NewApi")
			@Override
		      public boolean onDrag(View v, DragEvent event) {
		      // TODO Auto-generated method stub
		      final int action = event.getAction();
		      
		            switch(action) {
		            case DragEvent.ACTION_DRAG_STARTED:
		            	break;
		            case DragEvent.ACTION_DRAG_EXITED:
		            	break;
		            case DragEvent.ACTION_DRAG_ENTERED:
		    	  		break;
		            case DragEvent.ACTION_DROP:{

		            	if (payer_remove_index == -1) {
		            		
		            	Log.v("My Log" ,"Drop right XianA!!!!!!!!!");
			    		for(int i=0;i<no_of_payers;i++){
				    		 if(tempb.getText().toString().equals(payers_name[i]))
				    		 {
//				    			  payers_payment_due[i]+=drop_item_amount;
//				    			  TextView tv_amt=(TextView)tableRows.get(i).findViewById(R.id.tv_payer_amount_who_had_the_lobster);
//				    			  tv_amt.setText(PaymentSettings.CURRENCY_SIGN+payers_payment_due[i]);
//				    			  adapter.remove(adapter.getItem(drop_item_remove_index));
//				    			  adapter.notifyDataSetChanged();
				    			 
				    			boolean isReviewed = false;
				    			for (int jj = 0; jj < payers_order.get(i).all_items.size(); jj++) {
				    				if (payers_order.get(i).all_items.get(jj).item_name.equals(adapter.getItem(drop_item_remove_index).item_name)) {
				    					isReviewed = true;
				    					payers_order.get(i).all_items.get(jj).units.plusM(adapter.getItem(drop_item_remove_index).units);
				    				}
				    			}
				    			
				    			if (!isReviewed) {
				    				ItemDescDS newItem = new ItemDescDS();
				    				newItem.item_name      = adapter.getItem(drop_item_remove_index).item_name;
				    				newItem.price_per_unit = adapter.getItem(drop_item_remove_index).price_per_unit;
				    				newItem.units.plusM(adapter.getItem(drop_item_remove_index).units);
				    				payers_order.get(i).all_items.add(newItem);
				    			}
				    				
				    			  payers_payment_due[i]+=removeItem(drop_item_remove_index);
				    			  payers_payment_due[i]=(double) Math.round(payers_payment_due[i] * 100) / 100;
				    			  
				    			  int real_index = i;
				    			  for (int ii = 0; ii < i; ii++) {
				    				  if (payers_delete_bool[ii]) {
				    					  real_index --;
				    				  }
				    			  }
				    			  TextView tv_amt=(TextView)tableRows.get(real_index).findViewById(R.id.tv_payer_amount_who_had_the_lobster);
				    			  tv_amt.setText(PaymentSettings.CURRENCY_SIGN+payers_payment_due[i]); 
				    			  
				    			  final Button tempBGButton = (Button) tableRows.get(real_index).findViewById(R.id.bt_payer_name_who_had_the_lobster);
				    			  final Drawable buttonBackground = tempBGButton.getBackground();
				    			 	tempBGButton.setBackgroundResource(R.drawable.button_flat_red_normal);
				    			  final Handler handler = new Handler(); 
				    			  handler.postDelayed(new Runnable() { 
				    				    @Override 
				    				    public void run() { 
				    				        // Do something after 5s = 5000ms 
				    				    	tempBGButton.setBackgroundDrawable(buttonBackground);
				    				    } 
				    				}, 200);
			    			  
				    			  if(adapter.getCount()==0)
				    				  setAllButtonsBackgroundToDefault();
				    			  
				    			  MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.ding);
				    			  mp.start();
				    			  
				    			  break;
				    		    };
				    	 }
		            	} else {
			    		/////
		            	System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< : button");
		            	
		            	Log.v("My Log" ,"Drop left XianA!!!!!!!!!");
		            	
			    		for(int i=0;i<tableRows.size();i++){
							Button bt_temp_payer=(Button)tableRows.get(i).findViewById(R.id.bt_payer_name_who_had_the_lobster);
							
							if(bt_temp_payer.equals(v)  )
								{
										TextView tempet=(TextView)tableRows.get(i).findViewById(R.id.tv_payer_amount_who_had_the_lobster);
										TextView tempname=(TextView)tableRows.get(i).findViewById(R.id.bt_payer_name_who_had_the_lobster);
										TextView tempet2=(TextView)tableRows.get(payer_remove_index).findViewById(R.id.tv_payer_amount_who_had_the_lobster);
										Double personamountnext=Double.parseDouble(tempet2.getText().toString().replace(PaymentSettings.CURRENCY_SIGN,""));
										
										Double personamount= Double.parseDouble(tempet.getText().toString().replace(PaymentSettings.CURRENCY_SIGN,"")) + personamountnext;
										tempet.setText(PaymentSettings.CURRENCY_SIGN+(personamount));
										
										payers_payment_due[payer_remove_index] = 0;
										payers_delete_bool[payer_remove_index] = true;
										payers_payment_due[i] = payers_payment_due[i] + personamount;
										//System.out.println("Size : " + tableRows.size() + "  Index "+ i);										
										
										for (int i1=0;i1<no_of_payers;i1++) {
											if (payers_name[i1].equals(payers_order.get(i1).payer_name)) {
												Order nOrder = payers_order.get(i1);
												Order oOrder = payers_order.get(payer_remove_index);
												for (int i2=0;i2<oOrder.all_items.size();i2++) {
													boolean isChecked = false;
													for (int i3=0;i3<nOrder.all_items.size();i3++) {
														if (oOrder.all_items.get(i2).item_name.equals(nOrder.all_items.get(i3).item_name)) {
															isChecked = true;
															payers_order.get(i1).all_items.get(i3).units.plus(oOrder.all_items.get(i2).units);
														}
													}
													if (!isChecked) {
														payers_order.get(i1).all_items.add(oOrder.all_items.get(i2).copy());
													}
												}
											}
										}
										
//						    			ItemDescDS newItem = new ItemDescDS();
//						    			newItem.item_name      = adapter.getItem(drop_item_remove_index).item_name;
//						    			newItem.price_per_unit = adapter.getItem(drop_item_remove_index).price_per_unit;
//						    			newItem.total_price    = (double) Math.round(newItem.total_price + adapter.getItem(drop_item_remove_index).price_per_unit*100) /100;
//						    			newItem.units.integer = 1;
//						    			payers_order.get(i).all_items.add(newItem);
						    			
						    			payers_name[i] = payers_name[i] + " & " + (payer_remove_index+1);
						    			tempname.setText(payers_name[i]);
						    			
										table_layout.removeViewAt(payer_remove_index);	
										tableRows.remove(payer_remove_index);
										
										MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.ding);
						    			mp.start();
						    			
								}
						}
			    		payer_remove_index = -1;
			    		////
		        	//	Toast.makeText(WhoHadTheLobster.this,"Finaly Droped", Toast.LENGTH_LONG).show();
		            	
		                return(true);
		            	}
		            }
		            case DragEvent.ACTION_DRAG_ENDED:
		            	
		            	break;
		            default:
		            	break;
		            }
		      return true;
		      }});
		
		
    }
	
	public double removeItem(int index){
		
		double amount=0.0;
		if(adapter.getItem(index).units.value()<=1){
			final Handler handler = new Handler(); 
			  handler.postDelayed(new Runnable() { 
				    @Override 
				    public void run() { 
				        // Do something after 5s = 5000ms 
				    	rl_tempFoodRow.setBackgroundColor(new Color().parseColor("#00bcd4"));
				    	adapter.remove(adapter.getItem(drop_item_remove_index));
				    } 
				}, 100);
			amount = adapter.getItem(index).price_per_unit * adapter.getItem(index).units.value();
		}else{
			adapter.getItem(index).units.integer --;
			adapter.getItem(index).total_price-=adapter.getItem(index).price_per_unit;
			amount = adapter.getItem(index).price_per_unit;
		}
				
		final Handler handler = new Handler(); 
		  handler.postDelayed(new Runnable() { 
			    @Override 
			    public void run() { 
			        // Do something after 5s = 5000ms 
			    	rl_tempFoodRow.setBackgroundColor(new Color().parseColor("#00bcd4"));
			    	adapter.notifyDataSetChanged();
			    } 
			}, 100); 
		
		return amount;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.who_had_the_lobster, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public void setAllButtonsBackgroundToDefault(){
		Button tempb;
		for(int i=0;i<tableRows.size();i++){
			tempb=(Button) tableRows.get(i).findViewById(R.id.bt_payer_name_who_had_the_lobster);
			tempb.setBackgroundResource(R.drawable.button_flat_green_purple);
		}
	}
	
	public class MyListAdapter extends ArrayAdapter<ItemDescDS>{

		private ArrayList<ItemDescDS> items;
		private int layoutResourceId;
		public MyListAdapter(Context context, int layoutResourceId, ArrayList<ItemDescDS> items) {
			super(context, layoutResourceId, items);
			this.layoutResourceId = layoutResourceId;
			this.items = items;
		}
		
		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflater = getLayoutInflater();
			final View row = inflater.inflate(layoutResourceId, parent, false);
			
			final int tempposition=position;
			row.setOnTouchListener(new View.OnTouchListener() {
				
				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					
					payer_remove_index = -1;
		    		rl_tempFoodRow=(RelativeLayout) v.findViewById(R.id.rl_food_items_row);
					rlBackground=rl_tempFoodRow.getBackground();
					
					Log.v("My Log" ,"Single Tap XianA!!!!!!!!!");
					
					 drop_item_remove_index=tempposition;
					 boolean dragornot=true;
			    	  for(int i=0;i<no_of_payers;i++){
			    		 if(payers_hilight[i]==true)
			    		 {
			    			 dragornot=false;
			    			 break;
			    		 }
			    	 }
			    	
			    	if(dragornot){
				        tempView=row;
						return myView.onTouchEvent(event);
				        
			    	}else{

						rl_tempFoodRow.setBackgroundColor(Color.GREEN);
						
	    			 	
 			    		int cnt=0;
			    		for(int i=0;i<no_of_payers;i++){
				    		 if(payers_hilight[i]==true)
				    		    cnt++;
				    	 }
			    		
						for (int i1=0;i1<no_of_payers;i1++) {
							if(payers_hilight[i1]==true) {
								Order nOrder = payers_order.get(i1);
								ItemDescDS removeItem = adapter.getItem(tempposition).copy();
								
								boolean isChecked = false;
								for (int i3=0;i3<nOrder.all_items.size();i3++) {
									if (removeItem.item_name.equals(nOrder.all_items.get(i3).item_name)) {
										isChecked = true;
										payers_order.get(i1).all_items.get(i3).units.plus(removeItem.units.divideN(cnt));
									}
								}
								if (!isChecked) {
									ItemDescDS newItem = removeItem.copy();
									newItem.units.divide(cnt);
									payers_order.get(i1).all_items.add(newItem);
								}
							}
						}
						
						
						double amountdue=removeItem(tempposition);
						
			    		for(int i=0;i<no_of_payers;i++){
			    		 if(payers_hilight[i]==true)
			    		    {
			    			  payers_payment_due[i]+=amountdue/cnt;
			    			  payers_payment_due[i]=(double) Math.round(payers_payment_due[i] * 100) / 100;
			    			  
			    			  int real_index = i;
			    			  for (int ii = 0; ii < i; ii++) {
			    				  if (payers_delete_bool[ii]) {
			    					  real_index --;
			    				  }
			    			  }
			    			  
			    			  TextView tv_amt=(TextView)tableRows.get(real_index).findViewById(R.id.tv_payer_amount_who_had_the_lobster);
			    			  tv_amt.setText(PaymentSettings.CURRENCY_SIGN+payers_payment_due[i]);

			    		    }
			    	     }
		    			if(adapter.getCount()==0)
	    				  setAllButtonsBackgroundToDefault();
			    		
			    		return false;
			    	}
				}
			});
			
			TextView tv_int = (TextView)row.findViewById(R.id.tv_item_int);
			TextView tv_num = (TextView)row.findViewById(R.id.tv_item_num);
			TextView tv_den = (TextView)row.findViewById(R.id.tv_item_den);
			TextView tv_sla = (TextView)row.findViewById(R.id.tv_item_sla);
			TextView tv_name = (TextView)row.findViewById(R.id.tv_item_name);

			TextView tv_amount = (TextView)row.findViewById(R.id.tv_item_rate);
			
			tv_int.setText(""+items.get(position).units.integer);
			tv_num.setText(""+items.get(position).units.numerator);
			tv_den.setText(""+items.get(position).units.denominator);
			if (items.get(position).units.integer == 0) {
				tv_int.setVisibility(View.GONE);
			}
			
			if (items.get(position).units.numerator == 0) {
				tv_num.setVisibility(View.GONE);
				tv_den.setVisibility(View.GONE);
				tv_sla.setVisibility(View.GONE);
			}
			tv_name.setText(items.get(position).item_name);
			items.get(position).total_price = (double) Math.round(items.get(position).total_price * 100) / 100;
			tv_amount.setText(PaymentSettings.CURRENCY_SIGN+items.get(position).total_price);

			return row;
			
		}

	}

	////////////////////////////////////////////////////////////////////////
	
	
	public class MyView extends View {
		 
		GestureDetector gestureDetector;
		 
		public MyView(Context context) {
		    super(context);
		            // creating new gesture detector 
		    gestureDetector = new GestureDetector(context, new GestureListener());
		} 
		 
		// skipping measure calculation and drawing 
		 
		    @SuppressLint("ClickableViewAccessibility")
		// delegate the event to the gesture detector 
		@Override 
		public boolean onTouchEvent(MotionEvent e) {
		    return gestureDetector.onTouchEvent(e);
		} 
		 
		 
		private class GestureListener extends GestureDetector.SimpleOnGestureListener{
			
			@Override
			public boolean onDown(MotionEvent e) {
				// TODO Auto-generated method stub
				return true;
			}
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO Auto-generated method stub
				// xian
				payer_remove_index = -1;
				Log.v("My Log" ,"Double Tap XianA!!!!!!!!!");
				rl_tempFoodRow.setBackgroundColor(Color.GREEN);
				
				for (int i1=0;i1<no_of_payers;i1++) {
					Order nOrder = payers_order.get(i1);
					ItemDescDS removeItem = adapter.getItem(drop_item_remove_index).copy();
					
					boolean isChecked = false;
					for (int i3=0;i3<nOrder.all_items.size();i3++) {
						if (removeItem.item_name.equals( nOrder.all_items.get(i3).item_name)) {
							isChecked = true;
							payers_order.get(i1).all_items.get(i3).units.plus(removeItem.units.divideN(no_of_payers));
						}
					}
					
					if (!isChecked) {
						ItemDescDS newObj = removeItem.copy();
						newObj.units.divide(no_of_payers);
						payers_order.get(i1).all_items.add(newObj);
					}
				}
				
		        double amountdue=removeItem(drop_item_remove_index)/no_of_payers;
	    		for(int i=0;i<no_of_payers;i++){
	    			  payers_payment_due[i]+=amountdue;
	    			  payers_payment_due[i]=(double) Math.round(payers_payment_due[i] * 100) / 100;
	    			  
	    			  int real_index = i;
	    			  for (int ii = 0; ii < i; ii++) {
	    				  if (payers_delete_bool[ii]) {
	    					  real_index --;
	    				  }
	    			  }
	    			  TextView tv_amt=(TextView)tableRows.get(real_index).findViewById(R.id.tv_payer_amount_who_had_the_lobster);
	    			  tv_amt.setText(PaymentSettings.CURRENCY_SIGN+payers_payment_due[i]);
	    	    }
				
				return false;//super.onDoubleTap(e);
			}
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				Log.v("My Log","Single Tap");
				return true;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				
				Log.v("My Log>>>>>>>>>>>>>>>>>>>>>>>>","Single Tap up");
				
				return false;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
					float distanceY) {
				// TODO Auto-generated method stub
				return false;
			}
 
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				// TODO Auto-generated method stub

			}
			
			
			@Override
			public void onShowPress(MotionEvent e) {
				// TODO Auto-generated method stub
				
				payer_remove_index = -1;
				ClipData data = ClipData.newPlainText("DRAG", "");
		        View.DragShadowBuilder shadow = new View.DragShadowBuilder(tempView);
		        TextView tv_itemQyantity=(TextView)shadow.getView().findViewById(R.id.tv_item_int);
		        String itemQTYText= tv_itemQyantity.getText().toString();
	
//		        double realValue = 1;
//		        for (int i=0; i < adapter.getCount(); i++) {
//		        	TextView tv_itemName=(TextView)shadow.getView().findViewById(R.id.tv_item_name);
//		        	if (adapter.getItem(i).item_name == tv_itemName.getText().toString()) {
//		        		realValue = adapter.getItem(i).units.value();
//		        	}
//		        }
//		         TextView tv_amount=(TextView)shadow.getView().findViewById(R.id.tv_item_rate);
//		         String amountText=tv_amount.getText().toString();
//		         double amount=Double.parseDouble(amountText.substring(1+amountText.indexOf(PaymentSettings.CURRENCY_SIGN)))   ;
//		         amount=(double) Math.round(amount * 100) / 100;
//		         tv_amount.setText(PaymentSettings.CURRENCY_SIGN+amount);
//		         tv_itemQyantity.setText("1");
//				 Log.v("My Log","Drag Started here");
				 tempView.startDrag(data, shadow, null, 0);
				 
//				 tv_itemQyantity.setText(itemQTYText);
//				 tv_amount.setText(amountText);
			}
		}
 
	} 

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
		System.out.println("Here is result");
	}
	
	/////////////Main Activity Ends
	
	
}
