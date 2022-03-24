/*******************************************************************************
* Copyright (c) 2019 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.debug.core.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.java.debug.core.JdiExceptionReference;

public class ExceptionManager implements IExceptionManager {
    private Map<Long, JdiExceptionReference> exceptions = Collections.synchronizedMap(new HashMap<>());

    @Override
    public JdiExceptionReference getException(long threadId) {
        return exceptions.get(threadId);
    }

    @Override
    public JdiExceptionReference removeException(long threadId) {
        return exceptions.remove(threadId);
    }

    @Override
    public JdiExceptionReference setException(long threadId, JdiExceptionReference exception) {
        return exceptions.put(threadId, exception);
    }

    @Override
    public void removeAllExceptions() {
        exceptions.clear();
    }
}
