package com.restaurant.data.enums;

import java.io.Serializable;

/**
 * .
 *
 * @author namdx.
 * @created 1 /9/2024 - 1:34 PM.
 */
public interface IBaseErrorCode extends Serializable {
    /**
     * Gets error code.
     *
     * @return the error code
     */
    String getErrorCode();

    /**
     * Gets message code.
     *
     * @return the message code
     */
    String getMessageCode();

    /**
     * Gets http status code.
     *
     * @return the http status code
     */
    int getHttpStatusCode();
}
