/*******************************************************************************
* Copyright (c) 2017 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.debug.core.adapter;

import java.util.concurrent.CompletableFuture;

import com.microsoft.java.debug.core.protocol.Messages;

public interface IDebugAdapter {
    CompletableFuture<Messages.Response> dispatchRequest(Messages.Request request);
}
