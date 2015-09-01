package multipayerpaymentscreens;

import java.util.ArrayList;

import payments.PaymentSettings;
import serverutil.Fraction;
import serverutil.HandleCoupons;
import serverutil.ItemDescDS;

import com.example.hotelproject.R;

import databaseutil.DBHelperFraction;
import databaseutil.FractionRowDS;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TipCouponMultipayer extends ActionBarActivity {

	
	final Context context = this;
	
	Button bt_show_amount,bt_total_amount,bt_tipamountabsolute,bt_agreepay;
	EditText et_custom_tip;
	double billAmount,totaldue,tipAmount,discount_amount;
	ImageView iv_couponcode;
	TextView tv_coupon_applied;
	int order_id;
	String payer_name;
	String who_is_paying;
	TableLayout table_layout;
	DBHelperFraction dBHelperFraction;
	ArrayList<FractionRowDS> fraction_list;
	ArrayList<ItemDescDS> desc_list;
	ArrayList<ItemDescDS> empty_list;
	
	private void LoadData(){   

		Bundle extraBundle;
		   extraBundle=getIntent().getBundleExtra("databundle");
		   order_id=extraBundle.getInt("order_id");
		   billAmount=extraBundle.getDouble("totaldue");
		   payer_name=extraBundle.getString("payer_name");
		   who_is_paying=extraBundle.getString("who_is_paying");
		   tipAmount=0;
		   discount_amount=0;
		   
		   bt_show_amount=(Button)findViewById(R.id.bt_mp_amount_tipcoupon);
		   bt_total_amount=(Button)findViewById(R.id.bt_mp_totaldue);
		   bt_tipamountabsolute=(Button)findViewById(R.id.bt_mp_tipamountabsolute);
		   bt_agreepay=(Button)findViewById(R.id.bt_mp_agreeandpay);
		   
		   et_custom_tip=(EditText)findViewById(R.id.et_mp_custom_tip_amount);
		   
		   tv_coupon_applied=(TextView)findViewById(R.id.tv_mp_coupon_applied_tipcoupon);
		   
		   iv_couponcode=(ImageView)findViewById(R.id.iv_mp_coupon_tipcoupon);

		   if (!who_is_paying.equals("who_had_the_lobster")) return;
		   dBHelperFraction = new DBHelperFraction(this);
		   fraction_list = dBHelperFraction.getAllPersons(order_id);
		   desc_list = new ArrayList<ItemDescDS>();
		   empty_list = new ArrayList<ItemDescDS>();
		   System.out.println(fraction_list.size()+":::::");
		   for (int k=0; k<fraction_list.size(); k++) {
			  FractionRowDS fractionItem = fraction_list.get(k);
			  if (fractionItem.person_name.equals(payer_name)) {
				  ItemDescDS descObj = new ItemDescDS();
				  descObj.item_name = fractionItem.item_name;
				  descObj.units = new Fraction(fractionItem.units, fractionItem.f_up, fractionItem.f_down);
				  descObj.price_per_unit = fractionItem.price;
				  desc_list.add(descObj);
			  } else if (fractionItem.person_name.equals("empty")) {
				  ItemDescDS emptyObj = new ItemDescDS();
				  emptyObj.item_name = fractionItem.item_name;
				  emptyObj.units = new Fraction(fractionItem.units, fractionItem.f_up, fractionItem.f_down);
				  emptyObj.price_per_unit = fractionItem.price;
				  empty_list.add(emptyObj);
			  }
		   }
		   // review table view section
		   table_layout=(TableLayout)findViewById(R.id.tbl_review);
		   table_layout.removeAllViews();
		   
		   for(int ii=0;ii<desc_list.size();ii++) {
				final int iii = ii;
				final TableRow tr=(TableRow)LayoutInflater.from(TipCouponMultipayer.this).inflate(R.layout.payer_review_row, null);
				final TextView tv_text = (TextView)tr.findViewById(R.id.tv_text);
				final TextView tv_int = (TextView)tr.findViewById(R.id.tv_int);
				final TextView tv_num = (TextView)tr.findViewById(R.id.tv_num);
				final TextView tv_den = (TextView)tr.findViewById(R.id.tv_den);
				final TextView tv_sla = (TextView)tr.findViewById(R.id.tv_sla);
				double leftVal = desc_list.get(ii).units.value();
				
				double currency_val = desc_list.get(ii).price_per_unit*leftVal;
				double rounded = (double)Math.round(currency_val*100)/100;
				tv_text.setText("Btl. "+desc_list.get(ii).item_name+" "+PaymentSettings.CURRENCY_SIGN+rounded);
				tv_int.setText(""+desc_list.get(ii).units.integer);
				tv_num.setText(""+desc_list.get(ii).units.numerator);
				tv_den.setText(""+desc_list.get(ii).units.denominator);
				if (desc_list.get(ii).units.integer == 0) {
					tv_int.setVisibility(View.INVISIBLE);
				}
				if (desc_list.get(ii).units.numerator == 0) {
					tv_num.setVisibility(View.GONE);
					tv_den.setVisibility(View.GONE);
					tv_sla.setVisibility(View.GONE);
				}
				Button minusBtn = (Button)tr.findViewById(R.id.bt_minus);
				minusBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
//						desc_list.get(iii).units.integer --;
						boolean isChecked = false;
						double adjust = 0.0;
						for (int jj=0; jj<empty_list.size(); jj++) {
							if (empty_list.get(jj).item_name.equals(desc_list.get(iii).item_name)) {
								isChecked = true;
								if (desc_list.get(iii).units.value() > 1) {
									empty_list.get(jj).units.integer ++;
									desc_list.get(iii).units.integer --;
									adjust = desc_list.get(iii).price_per_unit; 
								} else {
									empty_list.get(jj).units.plus(desc_list.get(iii).units);
									adjust = desc_list.get(iii).units.value()*desc_list.get(iii).price_per_unit;
									desc_list.get(iii).units.integer = 0;
									desc_list.get(iii).units.numerator = 0;
									desc_list.get(iii).units.denominator = 1;
								}
							}
						}
						if (!isChecked) {
							if (desc_list.get(iii).units.value() > 1) {
								ItemDescDS nItem = desc_list.get(iii).copy();
								nItem.units = new Fraction(1, 0, 1);
								empty_list.add(nItem);
								desc_list.get(iii).units.integer --;
								adjust = desc_list.get(iii).price_per_unit;
							} else {
								empty_list.add(desc_list.get(iii).copy());
								adjust = desc_list.get(iii).units.value()*desc_list.get(iii).price_per_unit;
								desc_list.get(iii).units.integer = 0;
								desc_list.get(iii).units.numerator = 0;
								desc_list.get(iii).units.denominator = 1;
							}
						}
						
						double leftVal1 = desc_list.get(iii).units.value();
						double currency_val1 = desc_list.get(iii).price_per_unit*leftVal1;
						double rounded1 = (double)Math.round(currency_val1*100)/100;
						
						tv_text.setText("Btl. "+desc_list.get(iii).item_name+" "+PaymentSettings.CURRENCY_SIGN+rounded1);
						tv_int.setText(""+desc_list.get(iii).units.integer);
						tv_num.setText(""+desc_list.get(iii).units.numerator);
						tv_den.setText(""+desc_list.get(iii).units.denominator);
						if (desc_list.get(iii).units.integer == 0) {
							tv_int.setVisibility(View.INVISIBLE);
						}
						if (desc_list.get(iii).units.numerator == 0) {
							tv_num.setVisibility(View.GONE);
							tv_den.setVisibility(View.GONE);
							tv_sla.setVisibility(View.GONE);
						}
						
						if (desc_list.get(iii).units.value() == 0.0) {
							System.out.println(""+iii+":"+desc_list.size());
//							table_layout.removeViewAt(iii);
							tr.setVisibility(View.GONE);
//							desc_list.remove(iii);
						}
						
						billAmount-=adjust;
						billAmount=(double) Math.round(billAmount * 100) / 100;
						bt_show_amount.setText("Total: "+PaymentSettings.CURRENCY_SIGN + billAmount);
						
						tipAmount=(billAmount*15)/100;
						totaldue=(billAmount+tipAmount);
						totaldue=(double) Math.round(totaldue * 100) / 100;
						bt_total_amount.setText("Total Due: "+ PaymentSettings.CURRENCY_SIGN+totaldue);

					}
					
				});
				Button plusBtn = (Button)tr.findViewById(R.id.bt_plus);
				plusBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						double adjust = 0.0;
						for (int jj=0; jj<empty_list.size(); jj++) {
							if (empty_list.get(jj).item_name.equals(desc_list.get(iii).item_name)) {
								if (empty_list.get(jj).units.value() > 1) {
									empty_list.get(jj).units.integer --;
									desc_list.get(iii).units.integer ++;
									adjust = desc_list.get(iii).price_per_unit;
								} else {
									desc_list.get(iii).units.plus(empty_list.get(jj).units);
									adjust = empty_list.get(jj).units.value()*empty_list.get(jj).price_per_unit;
									empty_list.get(jj).units = new Fraction(0, 0, 1);
								}
							}
						}
						
						double leftVal1 = desc_list.get(iii).units.value();
						double currency_val1 = desc_list.get(iii).price_per_unit*leftVal1;
						double rounded1 = (double)Math.round(currency_val1*100)/100;
						
//						textBtn.setText(desc_list.get(iii).units.integer+":"+desc_list.get(iii).units.numerator+"/"+desc_list.get(iii).units.denominator+" "+desc_list.get(iii).item_name+" "+PaymentSettings.CURRENCY_SIGN+rounded1);
						tv_text.setText("Btl. "+desc_list.get(iii).item_name+" "+PaymentSettings.CURRENCY_SIGN+rounded1);
						tv_int.setText(""+desc_list.get(iii).units.integer);
						tv_num.setText(""+desc_list.get(iii).units.numerator);
						tv_den.setText(""+desc_list.get(iii).units.denominator);
						if (desc_list.get(iii).units.integer == 0) {
							tv_int.setVisibility(View.INVISIBLE);
						}
						if (desc_list.get(iii).units.numerator == 0) {
							tv_num.setVisibility(View.GONE);
							tv_den.setVisibility(View.GONE);
							tv_sla.setVisibility(View.GONE);
						}

						billAmount+=adjust;
						billAmount=(double) Math.round(billAmount * 100) / 100;
						bt_show_amount.setText("Total: "+PaymentSettings.CURRENCY_SIGN + billAmount);
						
						tipAmount=(billAmount*15)/100;
						totaldue=(billAmount+tipAmount);
						totaldue=(double) Math.round(totaldue * 100) / 100;
						bt_total_amount.setText("Total Due: "+ PaymentSettings.CURRENCY_SIGN+totaldue);
					}
					
				});

				table_layout.addView(tr);
				
		   }
	      // Toast.makeText(this,""+extraBundle.getDouble("totaldue"), Toast.LENGTH_LONG).show();
	   }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mp_tip_coupon);
		LoadData();
		
		billAmount=(double) Math.round(billAmount * 100) / 100;
		bt_show_amount.setText("Total: "+PaymentSettings.CURRENCY_SIGN + billAmount);
		
		tipAmount=(billAmount*15)/100;
		totaldue=(billAmount+tipAmount);
		totaldue=(double) Math.round(totaldue * 100) / 100;
		bt_total_amount.setText("Total Due: "+ PaymentSettings.CURRENCY_SIGN+totaldue);

		bt_tipamountabsolute.setText("Tip @ 15%");
		bt_tipamountabsolute.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PopupMenu popupMenu = new PopupMenu(TipCouponMultipayer.this, v);
				popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						// TODO Auto-generated method stub
						////////////////////////////
						
									switch (arg0.getItemId()) {
									case R.id.tip_rate_5:
										tipAmount=(billAmount*5)/100;
										totaldue=(billAmount+tipAmount-discount_amount);
										setAmountTipButtonsText(totaldue,5);
										break;
									case R.id.tip_rate_10:
										tipAmount=(billAmount*10)/100;
										totaldue=(billAmount+tipAmount-discount_amount);
										setAmountTipButtonsText(totaldue,10);;
										break;
									case R.id.tip_rate_15:
										tipAmount=(billAmount*15)/100;
										totaldue=(billAmount+tipAmount-discount_amount);
										setAmountTipButtonsText(totaldue,15);;
										break;
									case R.id.tip_rate_20:
										tipAmount=(billAmount*20)/100;
										totaldue=(billAmount+tipAmount-discount_amount);
										setAmountTipButtonsText(totaldue,20);
										break;
									case R.id.tip_rate_25:
										tipAmount=(billAmount*25)/100;
										totaldue=(billAmount+tipAmount-discount_amount);
										setAmountTipButtonsText(totaldue,25);
										break;
									}
									
						///////////////////////////
						return false;
					}
				});
				
				popupMenu.inflate(R.menu.rate_popup_menu);
				popupMenu.show();
			}
		});

		et_custom_tip.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
			        	if(!et_custom_tip.getText().toString().equals(""))
			        	{ 
			        		double ct=Double.parseDouble(et_custom_tip.getText().toString());
			        		tipAmount=ct;
			        		totaldue=(billAmount+tipAmount-discount_amount);
			        		setAmountTipButtonsText(totaldue,0);
		            
			        	}
			        	else
			        		{
			        		tipAmount=0;
			        		setAmountTipButtonsText(billAmount,0);
			        		}
				}
	
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

		});
	
		
		iv_couponcode.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Dialog dialog =new Dialog(context);
				 
                //tell the Dialog to use the dialog.xml as it's layout description
                dialog.setContentView(R.layout.coupon_dialog);
                dialog.setTitle("Enter Coupon Code");
 
                final TextView tv_coupon_code = (TextView) dialog.findViewById(R.id.et_coupon_code);
 
                Button bt_apply = (Button) dialog.findViewById(R.id.bt_apply_coupon);
 
                bt_apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	
		                        dialog.dismiss();
		                        HandleCoupons handleCoupons=new HandleCoupons(tv_coupon_code.getText().toString());
		                        handleCoupons.fetchDataFromServer();
		                        while(handleCoupons.parsingComplete);
		                        
		                        if(handleCoupons.isCouponValid()){
		                        	
			                        	if(handleCoupons.isCouponAbsolute()){
			                        		double absolute_amount=handleCoupons.getDiscountUnit();
			                        		
				                        		if(billAmount<absolute_amount)
				                        		{
				                        			discount_amount=0;
				                            		totaldue=(billAmount-discount_amount+tipAmount);
				                        			bt_total_amount.setText("Total due: "+PaymentSettings.CURRENCY_SIGN+ totaldue);
				                        			tv_coupon_applied.setText("No Coupon Applied");
				                                	
				                        			AlertDialog.Builder alertDialog = new AlertDialog.Builder(TipCouponMultipayer.this);
				                                    alertDialog.setTitle("Error");
				                                    alertDialog.setMessage("This Coupon Code is Not Applicable...!");
				                                    alertDialog.setIcon(R.drawable.error);
				                                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				                                        public void onClick(DialogInterface dialog, int which) {
				                                        // User pressed YES button. Write Logic Here
				                                        }
				                                    	});
				                                    // Showing Alert Message
				                                    alertDialog.show();	
				                        		}
				                        		else{
				                        			discount_amount=absolute_amount;
				                        			totaldue=(billAmount-discount_amount+tipAmount);
				                        			tv_coupon_applied.setText("Coupon Applied \""+tv_coupon_code.getText() +"\" you got " + PaymentSettings.CURRENCY_SIGN+ discount_amount +" Discount");
				                        			bt_total_amount.setText("Total due: "+PaymentSettings.CURRENCY_SIGN+ totaldue);
				                        		}
			                        	}
			                        	else{
			                        		tv_coupon_applied.setText("Coupon Applied \""+tv_coupon_code+"\" you got ");
			                        		discount_amount=(billAmount*handleCoupons.getDiscountUnit())/100;
			                        		totaldue=(billAmount-discount_amount+tipAmount);
			                        		tv_coupon_applied.setText("Coupon Applied \""+tv_coupon_code.getText()+"\" you got " + PaymentSettings.CURRENCY_SIGN+ discount_amount +" Discount");
			                    			bt_total_amount.setText("Total due: "+PaymentSettings.CURRENCY_SIGN+ totaldue);
			                        	}
		                        	
		                        }else{
		                        	discount_amount=0;
		                    		totaldue=(billAmount-discount_amount+tipAmount);
		                			bt_total_amount.setText("Total due: "+PaymentSettings.CURRENCY_SIGN+ totaldue);
		                			tv_coupon_applied.setText("No Coupon Applied");
		                        	
		                			AlertDialog.Builder alertDialog = new AlertDialog.Builder(TipCouponMultipayer.this);
		                            alertDialog.setTitle("Error");
		                            alertDialog.setMessage("Invalid Coupon Code...!");
		                            alertDialog.setIcon(R.drawable.error);
		                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		                                public void onClick(DialogInterface dialog, int which) {
		                                // User pressed YES button. Write Logic Here
		                                }
		                            	});
		                            // Showing Alert Message
		                            alertDialog.show();	
		 
		                        }

                    }
                });
 
                dialog.show();
			}
		});
		
		
		bt_agreepay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ArrayList<FractionRowDS> fraction_list = dBHelperFraction.getAllPersons(order_id);
				dBHelperFraction.deleteEmptyData(order_id);
				ArrayList<FractionRowDS> fraction_list1 = dBHelperFraction.getAllPersons(order_id);
				for(int k=0;k<empty_list.size();k++){	
		    		ItemDescDS itemObj = empty_list.get(k);
		    		if (itemObj.units.value() > 0)
		    			dBHelperFraction.insertData(order_id, "empty", itemObj.item_name, itemObj.price_per_unit, itemObj.units.integer, itemObj.units.numerator, itemObj.units.denominator);
		    	}
				ArrayList<FractionRowDS> fraction_list2 = dBHelperFraction.getAllPersons(order_id);
				System.out.println(""+fraction_list.size()+":"+fraction_list1.size()+":"+fraction_list2.size());
				Bundle extrabundle = new Bundle();
				
				extrabundle.putDouble("totaldue",totaldue);
				extrabundle.putInt("order_id", order_id);
				extrabundle.putDouble("discount",discount_amount);
				extrabundle.putString("payer_name",payer_name);
				extrabundle.putString("who_is_paying", who_is_paying);
				Bundle bundle =new Bundle();
				bundle.putBundle("databundle", extrabundle);
				Intent myIntent = new Intent(TipCouponMultipayer.this, RecieptScreenMultiPayer.class);
				
				myIntent.putExtras(bundle);
				startActivity( myIntent);
			}
		});
		
	}

	public void setAmountTipButtonsText(double totaldue,int tip){
		totaldue=(double) Math.round(totaldue * 100) / 100;
		bt_total_amount.setText("Total due: "+PaymentSettings.CURRENCY_SIGN+ totaldue);
		if(tip==0)
			bt_tipamountabsolute.setText("Flat Rate");
		else{
	    	bt_tipamountabsolute.setText("Tip @ "+tip+"%");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tip_coupon_multipayer, menu);
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
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
