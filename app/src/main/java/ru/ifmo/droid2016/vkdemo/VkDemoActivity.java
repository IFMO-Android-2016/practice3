package ru.ifmo.droid2016.vkdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKScopes;

public class VkDemoActivity extends AppCompatActivity {

    private TextView nameView;
    private SimpleDraweeView imageView;
    private ProgressBar progressView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();

        // Код, который проверяет -- есть ли у нас ранее
        // сохраненный токен -- и если нет, то инициирует процедуру авторизации в Vk SDK.
        // А если есть, то сразу вызывает onLoggedIn
        VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, Constants.KEY_TOKEN);
        if (token != null) {
            Log.d(TAG, "onCreate: using saved token");
            onLoggedIn(token);

        } else if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: token is missing, performing login...");
            VKSdk.login(this, VKScopes.PHOTOS);
        }
    }

    protected void initContentView() {
        setContentView(R.layout.activity_vk_demo);
        nameView = (TextView) findViewById(R.id.user_name);
        imageView = (SimpleDraweeView) findViewById(R.id.user_photo);
        progressView = (ProgressBar) findViewById(R.id.progress);
        logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(logoutClickListener);
    }

    /**
     * Сбрасывает все вьюшки в исходное состояние
     */
    void resetView() {
        logoutButton.setEnabled(false);
        progressView.setVisibility(View.GONE);
        nameView.setText(null);
        imageView.setImageURI((String) null);
    }


    protected void onLoggedIn(VKAccessToken token) {
        Log.d(TAG, "onLoggedIn: " + token);
        Toast.makeText(this, R.string.login_successful, Toast.LENGTH_LONG).show();

        // TODO: Task 3 - начать загрузку информации о текущем пользователе
    }

    protected void onLoginFailed(VKError error) {
        Log.w(TAG, "onLoginFailed: " + error);
        Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken token) {
                token.saveTokenToSharedPreferences(VkDemoActivity.this, Constants.KEY_TOKEN);
                onLoggedIn(token);
            }

            @Override
            public void onError(VKError error) {
                onLoginFailed(error);
            }
        });
    }

    /**
     * Обработчик клика на кнопку "Выйти", выполняет процедуру logout
     */
    private View.OnClickListener logoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Context context = VkDemoActivity.this;

            // Выполняем логаут в Vk SDK
            VKSdk.logout();

            // Удаляем сохраненный токен
            VKAccessToken.removeTokenAtKey(context, Constants.KEY_TOKEN);

            // Очищаем вьюшки
            resetView();

            // Отменяем загрузку для текущего пользователя
            getSupportLoaderManager().destroyLoader(0);

            // Выкидываем пользователя на стартовый экран
            finish();
            startActivity(new Intent(VkDemoActivity.this, MainActivity.class));
        }
    };

    protected final String TAG = getClass().getSimpleName();
}
