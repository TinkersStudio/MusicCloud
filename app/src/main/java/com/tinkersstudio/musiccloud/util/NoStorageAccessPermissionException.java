package com.tinkersstudio.musiccloud.util;

/**
 * Created by anhnguyen on 2/12/17.
 */

public class NoStorageAccessPermissionException extends RuntimeException {
    public NoStorageAccessPermissionException () {
        super();
    }
    public NoStorageAccessPermissionException (String msg) {
        super(msg);
    }
}
