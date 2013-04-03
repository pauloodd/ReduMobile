package br.com.redumobile.gui;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;
import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import br.com.redumobile.R;
import br.com.redumobile.ReduMobile;
import br.com.redumobile.adapter.AnswerAdapter;
import br.com.redumobile.entity.Answer;
import br.com.redumobile.entity.User;
import br.com.redumobile.gui.LinkEnabledTextView.TextLinkClickListener;
import br.com.redumobile.gui.clickablespan.BlueClickableSpan;
import br.com.redumobile.gui.clickablespan.BoldBlueClickableSpan;
import br.com.redumobile.oauth.ReduClient;
import br.com.redumobile.util.BitmapManager;
import br.com.redumobile.util.DaemonThread;
import br.com.redumobile.util.DateFormatter;

public final class ActivityStatusedByUserDetailsActivity extends GDActivity {
	
	private ReduMobile application;
	public static Activity activity;
	private BitmapManager bitmapManager;
	
	private final int ITEM_GO_HOME = 0;
	private final int ITEM_COMPOSE = 1;
	private final int ITEM_UPDATE = 2;
	
	private ReduClient client;
	private final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
	private DateFormatter dateFormatter;
	private LoaderActionBarItem itemUpdate;
	private ListView listAnswers;
	private AnswerAdapter listAnswersAdapter;
	private final int RECOGNIZER_RESULT = 1234;
	private EditText txtPostText;
	private volatile String userFullName;
	private String userId;
	
	private ArrayList<Answer> answers;	
	private boolean firstStart;
	

	private void loadBreadcrumb() {
		new DaemonThread(new Runnable() {
			@Override
			public void run() {
				if (userFullName == null) {
					User user = client.getUser(userId);
					if (user == null) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										ActivityStatusedByUserDetailsActivity.this,
										"Algumas informações não puderam ser obtidas",
										Toast.LENGTH_LONG).show();
							}
						});
					} else {
						userFullName = user.getFirstName() + " "
								+ user.getLastName();
					}
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (userFullName != null) {
							ImageView imgUser = (ImageView) findViewById(R.id.activityStatusedByUserDetailsImgUser);
							imgUser.setVisibility(View.VISIBLE);

							TextView lblFullName = (TextView) findViewById(R.id.activityStatusedByUserDetailsLblFullName);
							lblFullName.setText(userFullName);
						}
					}
				});
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == RECOGNIZER_RESULT) {
				ArrayList<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				String oldText = txtPostText.getText().toString();

				txtPostText.setText(oldText
						+ (oldText.length() == 0 || oldText.endsWith(" ") ? ""
								: " ") + results.get(0) + " ");
				txtPostText.setSelection(txtPostText.length());
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		if (firstStart) {
			firstStart = false;
		} else {
			updateAnswers();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setActionBarContentView(R.layout.activity_statused_by_user_details_activity);

		getAB().setType(ActionBar.Type.Empty);

		application = ReduMobile.getInstance();
		
		client = ReduMobile.getInstance().getClient();

		final User activityUser = activity.getUser();
		final User activityStatusable = (User) activity.getStatusable();

		userId = activityUser.getLogin() != null ? activityUser.getLogin()
				: String.valueOf(activityUser.getId());
		userFullName = activityUser.getFirstName() + " "
				+ activityUser.getLastName();

		loadBreadcrumb();

		bitmapManager = new BitmapManager();

		dateFormatter = new DateFormatter(DATE_FORMAT);

		TextView lblCreationDate = (TextView) findViewById(R.id.activityStatusedByUserDetailsLblCreationDate);
		lblCreationDate.setText(dateFormatter.format(activity.getCreatedAt()));

		SpannableString info = null;

		String activityUserFullName = activityUser.getFirstName() + " "
				+ activityUser.getLastName();
		String activityStatusableFullName = activityStatusable.getFirstName()
				+ " " + activityStatusable.getLastName();
		String thumbUrl = activityUser.getThumbnails().get(0).getHref();

		ImageView imgUserThumb = (ImageView) findViewById(R.id.activityStatusedByUserDetailsImgUserThumb);

		bitmapManager.displayBitmapAsync(thumbUrl, imgUserThumb);

		if (activityUser.getLogin().equals(activityStatusable.getLogin())) {
			String infoText = activityUserFullName
					+ " comentou no seu próprio mural";

			info = new SpannableString(infoText);
			info.setSpan(new BoldBlueClickableSpan() {
				@Override
				public void onClick(View widget) {
					super.onClick(widget);

					openUserWall(activityUser);
				}
			}, 0, activityUserFullName.length(), 0);
			info.setSpan(new BlueClickableSpan() {
				@Override
				public void onClick(View widget) {
					super.onClick(widget);

					openUserWall(activityStatusable);
				}
			}, activityUserFullName.length() + 13, infoText.length(), 0);
		} else {
			String infoText = activityUserFullName + " comentou no mural de "
					+ activityStatusableFullName;

			info = new SpannableString(infoText);
			info.setSpan(new BoldBlueClickableSpan() {
				@Override
				public void onClick(View widget) {
					super.onClick(widget);

					openUserWall(activityUser);
				}
			}, 0, activityUserFullName.length(), 0);
			info.setSpan(new BlueClickableSpan() {
				@Override
				public void onClick(View widget) {
					super.onClick(widget);

					openUserWall(activityStatusable);
				}
			}, activityUserFullName.length() + 22, infoText.length(), 0);
		}

		TextView lblInfo = (TextView) findViewById(R.id.activityStatusedByUserDetailsLblInfo);
		lblInfo.setText(info, BufferType.SPANNABLE);
		lblInfo.setMovementMethod(LinkMovementMethod.getInstance());

		LinkEnabledTextView lblText = (LinkEnabledTextView) findViewById(R.id.activityStatusedByUserDetailsLblText);
		lblText.gatherLinksForText(activity.getText());
		lblText.setOnTextLinkClickListener(new TextLinkClickListener() {
			@Override
			public void onTextLinkClick(View textView, String clickedString) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(clickedString)));
			}
		});
		lblText.setMovementMethod(LinkMovementMethod.getInstance());

		
		if(activity.getAnswers() == null || activity.getAnswers().isEmpty()){
			ArrayList<Answer> listaResp = new ArrayList<Answer>();
			listaResp.add(new Answer(null, null, 0, "Sem Comentários", null, null));
			answers = listaResp;
		}else{
			answers = activity.getAnswers();
		}
		listAnswersAdapter = new AnswerAdapter(this, answers);
		
		firstStart = true;
		
		listAnswers = (ListView) findViewById(R.id.activityStatusedByUserDetailsListAnswers);
		listAnswers.setAdapter(listAnswersAdapter);

		addActionBarItem(ActionBarItem.Type.GoHome, ITEM_GO_HOME);
		itemUpdate = (LoaderActionBarItem) addActionBarItem(ActionBarItem.Type.Refresh, ITEM_UPDATE);
		addActionBarItem(ActionBarItem.Type.Compose, ITEM_COMPOSE);

	}

	private void openUserWall(User user) {
		Intent intent = new Intent(this, UserWallActivity.class);
		intent.putExtra("userId", user.getLogin());
		intent.putExtra("userFullName",
				user.getFirstName() + " " + user.getLastName());

		startActivity(intent);
	}
	
	private void updateAnswers() {
		itemUpdate.setLoading(true);

		auxUpdateAnswers();
	}
	
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		boolean clickHandled = true;

		int itemId = item.getItemId();
		if (itemId == ITEM_GO_HOME) {
			Intent intent = new Intent(this, UserWallActivity.class);
			intent.putExtra("userId", application.getUserLogin());

			startActivity(intent);
		} else if (itemId == ITEM_UPDATE) {
			updateAnswers();
		} else if (itemId == ITEM_COMPOSE) {
			Intent intent = new Intent(this, PostOnAnswer.class);
			 intent.putExtra("statusId", activity.getId());
			 intent.putExtra("lblName", "Responder em: "+userFullName);

			 startActivity(intent);
		} else {
			clickHandled = super.onHandleActionBarItemClick(item, position);
		}

		return clickHandled;
	}
	
	private void auxUpdateAnswers() {
		new DaemonThread(new Runnable() {
			@Override
			public void run() {
				
				Answer lastAnswer;
				if(answers != null && !answers.isEmpty()){
					lastAnswer = answers.get(answers.size() - 1);
				}else{
					lastAnswer = null;
				}
				
				if(lastAnswer != null && lastAnswer.getText().equals("Sem Comentários")){
					lastAnswer = null;
					answers.clear();
				}
				
				ArrayList<Answer> newAnswers = client.getAnswers(""+activity.getId());
				if (newAnswers != null) {
					for (int i = newAnswers.size() - 1; i >= 0; i--) {
						Answer answer = newAnswers.get(i);
						
						if(lastAnswer != null){
							if (answer.getUpdatedAt().before(
									lastAnswer.getUpdatedAt())) {
								break;
							} else if (answer.getUpdatedAt().after(
									lastAnswer.getUpdatedAt())) {
								answers.add(answer);
							}
						}else{
							answers.add(answer);
						}
						
					}
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(
									ActivityStatusedByUserDetailsActivity.this,
									"Algumas informações não puderam ser obtidas",
									Toast.LENGTH_LONG).show();
						}
					});
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listAnswersAdapter.notifyDataSetChanged();

						itemUpdate.setLoading(false);
					}
				});
			}
		}).start();
	}
}