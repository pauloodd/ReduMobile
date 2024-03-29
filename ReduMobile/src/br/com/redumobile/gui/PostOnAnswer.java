package br.com.redumobile.gui;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.redumobile.R;
import br.com.redumobile.ReduMobile;
import br.com.redumobile.entity.Status;
import br.com.redumobile.oauth.ReduClient;
import br.com.redumobile.util.DaemonThread;
import br.com.redumobile.util.Utils;

public final class PostOnAnswer extends GDActivity {
	private ReduClient client;
	private final int RECOGNIZER_RESULT = 1234;
	private EditText txtPostText;
	private volatile String lblName;
	private String statusId;

	private void loadBreadcrumb() {
		new DaemonThread(new Runnable() {
			@Override
			public void run() {
				if (lblName == null) {
					Status status = client.getStatus(statusId, false);
					if (status == null) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										PostOnAnswer.this,
										"Algumas informa��es n�o puderam ser obtidas",
										Toast.LENGTH_LONG).show();
							}
						});
					} else {
						lblName = status.getText();
					}
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (lblName != null) {
							ImageView img = (ImageView) findViewById(R.id.postOnAnswerImg);
							img.setVisibility(View.VISIBLE);

							TextView labelName = (TextView) findViewById(R.id.postOnAnswerLblName);
							labelName.setText(lblName);
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setActionBarContentView(R.layout.post_on_answer);

		getAB().setType(ActionBar.Type.Empty);

		client = ReduMobile.getInstance().getClient();

		statusId = getIntent().getStringExtra("statusId");
		if (statusId == null) {
			int id = getIntent().getIntExtra("statusId", -1);
			if (id == -1) {
				throw new RuntimeException("A id do status n�o foi informada");
			} else {
				statusId = String.valueOf(id);
			}
		}
		lblName = getIntent().getStringExtra("lblName");

		loadBreadcrumb();

		final ViewGroup lytSpinner = (ViewGroup) findViewById(R.id.postOnAnswerLytSpinner);

		final TextView lblTextInfo = (TextView) findViewById(R.id.postOnAnswerLblTextInfo);

		txtPostText = (EditText) findViewById(R.id.postOnAnswerTxtPostText);
		txtPostText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				int currentCharsCount = s.length();
				int remainingCharsCount = (int) Utils.clamp(
						ReduClient.MAX_CHARS_COUNT_IN_POST - currentCharsCount,
						0, ReduClient.MAX_CHARS_COUNT_IN_POST);

				lblTextInfo
						.setText(remainingCharsCount == 1 ? "1 caractere restante."
								: remainingCharsCount
										+ " caracteres restantes.");
			}
		});

		Button btnPost = (Button) findViewById(R.id.postOnAnswerBtnPost);
		btnPost.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (txtPostText.length() > 0
						&& txtPostText.length() <= ReduClient.MAX_CHARS_COUNT_IN_POST) {

					v.setVisibility(View.GONE);

					lytSpinner.setVisibility(View.VISIBLE);

					new DaemonThread(new Runnable() {
						@Override
						public void run() {
							final String posted = client.postStatusAnswer(statusId, txtPostText.getText().toString());
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (posted != null && !posted.equals("") && !posted.contains("not authorized")) {
										Toast.makeText(
												PostOnAnswer.this,
												"A��o realizada com sucesso",
												Toast.LENGTH_LONG).show();

										new Handler().postDelayed(
												new Runnable() {
													@Override
													public void run() {
														finish();
													}
												}, 3800);
									} else {
										if(posted.contains("not authorized")){
											Toast.makeText(
													PostOnAnswer.this,
													"A a��o desejada n�o p�de ser realizada, o usu�rio n�o autoriza postagens feitas por desconhecidos.",
													Toast.LENGTH_LONG).show();
										}else{
											Toast.makeText(
													PostOnAnswer.this,
													"A a��o desejada n�o p�de ser realizada. Tente novamente",
													Toast.LENGTH_LONG).show();
										}
									}

									lytSpinner.setVisibility(View.GONE);

									v.setVisibility(View.VISIBLE);

								}
							});
						}
					}).start();
				} else if (txtPostText.length() > ReduClient.MAX_CHARS_COUNT_IN_POST) {
					lblTextInfo.setText("Texto muito longo.");
				} else {
					lblTextInfo.setText("Texto deixado em branco.");
				}
			}
		});
	}
}