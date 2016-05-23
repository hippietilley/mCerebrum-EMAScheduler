package org.md2k.ema_scheduler.delivery;

import org.md2k.datakitapi.exception.DataKitException;

/**
 * Created by monowar on 3/13/16.
 */
public interface Callback {
    void onResponse(String response) throws DataKitException;
}
