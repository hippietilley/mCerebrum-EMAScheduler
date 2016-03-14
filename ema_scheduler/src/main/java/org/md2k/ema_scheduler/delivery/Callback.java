package org.md2k.ema_scheduler.delivery;

import org.md2k.utilities.data_format.NotificationRequest;

/**
 * Created by monowar on 3/13/16.
 */
public interface Callback {
    void onResponse(String response);
}
