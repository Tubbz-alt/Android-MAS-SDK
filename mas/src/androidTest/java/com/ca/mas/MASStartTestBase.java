/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import android.support.test.InstrumentationRegistry;

import com.ca.mas.core.io.IoUtils;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASDevice;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutionException;


public abstract class MASStartTestBase extends MASTestBase {

    private static final int DEFAULT_MAX_RESPONSE_SIZE = 10485760;

    @Before
    public void masStart() throws Exception {

        byte[] bytes = IoUtils.slurpStream(getClass().getResourceAsStream("/msso_config.json"), DEFAULT_MAX_RESPONSE_SIZE);
        JSONObject jsonObject = new JSONObject(new String(bytes));
        MAS.start(InstrumentationRegistry.getTargetContext(), jsonObject);
    }

    @After
    public void masStop() {
        MASCallbackFuture<Void> masCallback = new MASCallbackFuture<>();
        MASDevice.getCurrentDevice().deregister(masCallback);
        try {
            masCallback.get();
        } catch (Exception ignore) {
            //Ignore
        }
        MASDevice.getCurrentDevice().resetLocally();
        MAS.stop();
    }

}