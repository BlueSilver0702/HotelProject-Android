package multipayerpaymentscreens;

import java.util.ArrayList;

import payments.PaymentSettings;

import com.example.hotelproject.R;

import databaseutil.DBHelperWhoHadTheLobster;
import databaseutil.PersonRowDS;

import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CashPaymentScreenMultipayer extends ActionBarActivity {
	
	Button bt_proceed;
	EditText et_roundupto,et_leaving;
	Bundle extraBundle;
	TextView tv_amount,tv_dueback;
	double roundupto,totaldue,dueback,leaving;
	int order_id;
	String payer_name;
	DBHelperWhoHadTheLobster dBHelperWhoHadTheLobster;
	
	private void loadData(){   
		   
		   extraBundle=getIntent().getBundleExtra("databundle");
		   totaldue=extraBundle.getDouble("totaldue");
		   
		   SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences",Context.MODE_PRIVATE);
			order_id=sharedPreferences.getInt("order_id",0);                       
			payer_name=sharedPreferences.getString("payer_name", "");
		   
		   bt_proceed=(Button)findViewById(R.id.bt_mp_proceed_cash_payment_screen);
		   tv_amount=(TextView)findViewById(R.id.tv_mp_pleasepay_cash_payment_screen);
		   tv_dueback=(TextView)findViewById(R.id.tv_mp_cash_due_back_cashpaymentscreen);
		   et_roundupto=(EditText)findViewById(R.id.et_mp_roundupto_cash_payment_screen);
		   et_leaving=(EditText)findViewById(R.id.et_mp_leaving_cash_payment_screen);
		   
	       Toast.makeText(this,"Cash payment Screen : "+extraBundle.getDouble("totaldue"), Toast.LENGTH_LONG).show();
	       
	       dBHelperWhoHadTheLobster = new DBHelperWhoHadTheLobster(this);
	   }
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mp_cash_payment_screen);
		
		loadData();
		
		tv_amount.setText(PaymentSettings.CURRENCY_SIGN+ totaldue);
		
		if (totaldue > 100) {
			roundupto=totaldue-(totaldue%10)+10;
		} else {
			roundupto=totaldue-(totaldue%5)+5;
		}
		
		et_roundupto.setText(PaymentSettings.CURRENCY_SIGN+roundupto);
		et_roundupto.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				totaldue = roundupto;
				tv_amount.setText(PaymentSettings.CURRENCY_SIGN+ totaldue);
			}
			
		});
		dueback=0;
		tv_dueback.setText("Cash Due Back: "+PaymentSettings.CURRENCY_SIGN + dueback);
		et_leaving.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
		        	if(!et_leaving.getText().toString().trim().equals(""))
		        	{ 
		        		leaving=Double.parseDouble(et_leaving.getText().toString());
		        		if(leaving<totaldue){
		        			dueback=0;
		        			tv_dueback.setText("Cash Due Back: "+PaymentSettings.CURRENCY_SIGN + dueback);
		        		}else{
		        			dueback=leaving-totaldue;
		        			dueback = (double) Math.round(dueback * 100) / 100;
		        			tv_dueback.setText("Cash Due Back: "+PaymentSettings.CURRENCY_SIGN + dueback);
		        		}
	            
		        	}
		        	else
		        		{
		        		    leaving=0;
		        			dueback=0;
		        			tv_dueback.setText("Cash Due Back: "+PaymentSettings.CURRENCY_SIGN + dueback);
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
		
		bt_proceed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(leaving<totaldue)
					showAlertDialogBox();
				else {
					ArrayList<PersonRowDS> array_list=dBHelperWhoHadTheLobster.getAllPersons(order_id);
					dBHelperWhoHadTheLobster.deleteData(order_id);
					for (int i=0; i<array_list.size(); i++) {
						if (array_list.get(i).PERSON_NAME.equals(payer_name)) {
							array_list.get(i).PAID_AMOUNT = totaldue;
						}
						dBHelperWhoHadTheLobster.insertData(order_id, array_list.get(i).PERSON_NAME,array_list.get(i).PAID, array_list.get(i).PAID_AMOUNT);
					}
					
					
					
					
					Bundle bundle = new Bundle();
	            	extraBundle.putDouble("cash_due_back",dueback);
	        		bundle.putBundle("databundle",extraBundle);
	        		final Intent myIntent = new Intent(CashPaymentScreenMultipayer.this,SignatureScreenMultiPayer.class);
	        		myIntent.putExtras(bundle);
					startActivity( myIntent);
				}
				
			}
		});
		
	}

	private void showAlertDialogBox(){
			final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CashPaymentScreenMultipayer.this);
	     alertDialog.setTitle("Error");
	     alertDialog.setMessage("Input Valid Leaving Amount...!");
	     alertDialog.setIcon(R.drawable.error);
	     alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int which) {
	         }
	     	});
	     alertDialog.show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cash_payment_screen_multipayer, menu);
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
}
