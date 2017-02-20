/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.kapua.app.console.shared.model.authorization;

import org.eclipse.kapua.app.console.shared.model.GwtEntityCreator;

public class GwtAccessInfoCreator extends GwtEntityCreator {

    private static final long serialVersionUID = 7961041343683015932L;

    private GwtSubjectType subjectType;
    private String subjectId;

    public GwtAccessInfoCreator() {
        super();
    }

    public GwtSubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(GwtSubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

}