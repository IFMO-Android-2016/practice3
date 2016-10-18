package ru.ifmo.droid2016.vkdemo.loader.vk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiModel;

import ru.ifmo.droid2016.vkdemo.loader.LoadResult;
import ru.ifmo.droid2016.vkdemo.utils.IOUtils;

/**
 * Базовый загрузчик дял выполнения типизированного запроса к VK API. Параметр TData указывет
 * на тип результат, который получается после разбора ответа от АПИ при помощи парсера.
 */

public class VkApiRequestLoader<TData extends VKApiModel>
        extends AsyncTaskLoader<LoadResult<TData, VKError>> {

    public static final int VK_LOADER_ERROR = -201;

    /**
     * Запрос, который надо выполнить.
     */
    @NonNull
    private final VKRequest request;

    /**
     * Класс результата, который нужно получить.
     */
    @NonNull
    private final Class<TData> vkModelClass;

    public VkApiRequestLoader(@NonNull Context context,
                              @NonNull VKRequest request,
                              @NonNull Class<TData> vkModelClass) {
        super(context);
        this.request = request;
        this.vkModelClass = vkModelClass;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public LoadResult<TData, VKError> loadInBackground() {
        Log.d(TAG, "Start performing VK API request: " + request);

        final VKResponse[] responseRef = new VKResponse[1];
        final VKError[] errorRef = new VKError[1];

        request.setModelClass(vkModelClass);

        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                responseRef[0] = response;
            }

            @Override
            public void onError(VKError error) {
                errorRef[0] = error;
            }
        });

        if (responseRef[0] == null && errorRef[0] == null) {
            // Такого не должно быть вообще (если только баг VK SDK)
            return LoadResult.error(vkError("Loader has no result"));

        } else if (responseRef[0] != null && errorRef[0] != null) {
            // И такого тоже не должно быть (если только баг VK SDK)
            return LoadResult.error(vkError("Loader has ambiguous result"));

        } else if (errorRef[0] != null) {
            // Вот это уже какая-то реальная ошибка, а может просто нет интернета?
            if (!IOUtils.isConnectionAvailable(getContext(), true)) {
                return LoadResult.noInternet();
            } else {
                return LoadResult.error(errorRef[0]);
            }

        } else {
            // Более-менне нормальный результат
            final VKResponse response = responseRef[0];

            try {
                TData data = (TData) response.parsedModel;
                if (data == null) {
                    return LoadResult.error(vkError("Null parsed model"));
                } else {
                    return LoadResult.ok(data);
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse VK response: " + e, e);
                return LoadResult.error(vkError("Parser failure: " + e));
            }
        }
    }

    private static VKError vkError(String message) {
        final VKError error = new VKError(VK_LOADER_ERROR);
        error.errorMessage = message;
        return error;
    }


    private static final String TAG = "VkLoader";
}
