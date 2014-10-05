package com.yassirh.digitalocean.service;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.yassirh.digitalocean.data.AccountDao;
import com.yassirh.digitalocean.data.DatabaseHelper;
import com.yassirh.digitalocean.model.Account;
import com.yassirh.digitalocean.utils.ApiHelper;

public class AccountService {
	
	private Context context;
		
	public AccountService(Context context) {
		this.context = context;
	}
	
	
	public void getNewToken() {
		final Account currentAccount = ApiHelper.getCurrentAccount(context);
		if(currentAccount == null){
			return;
		}
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(String.format(Locale.US, "https://yassirh.com/digitalocean_swimmer/generate_refresh_token.php?refresh_token=%s", currentAccount.getRefreshToken()), new AsyncHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Calendar expiresIn = Calendar.getInstance();
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    expiresIn.add(Calendar.SECOND, Integer.parseInt(jsonObject.getString("expires_in")));
                    currentAccount.setExpiresIn(expiresIn.getTime());
                    currentAccount.setRefreshToken(jsonObject.getString("refresh_token"));
                    currentAccount.setToken(jsonObject.getString("access_token"));
                    AccountDao accountDao = new AccountDao(DatabaseHelper.getInstance(context));
                    accountDao.createOrUpdate(currentAccount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
			
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				if(statusCode == 401){
					ApiHelper.showAccessDenied();
				}
			}
			
		});
	}

	public boolean hasAccounts() {
		AccountDao accountDao = new AccountDao(DatabaseHelper.getInstance(context));
		List<Account> accounts = accountDao.getAll(null);
		return accounts.size() > 0;
	}
}