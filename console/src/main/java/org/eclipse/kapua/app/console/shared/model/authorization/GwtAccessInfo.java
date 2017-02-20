/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
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

import org.eclipse.kapua.app.console.shared.model.GwtEntityModel;

public class GwtAccessInfo extends GwtEntityModel {

    private static final long serialVersionUID = 1330881042880793119L;

    @Override
    @SuppressWarnings({ "unchecked" })
    public <X> X get(String property) {
        if ("subjectTypeEnum".equals(property)) {
            String subjectType = getSubjectType();
            if (subjectType != null)
                return (X) (GwtSubjectType.valueOf(subjectType));
            return (X) "";
        } else {
            return super.get(property);
        }
    }

    public String getSubjectType() {
        return get("subjectType");
    }

    public GwtSubjectType getSubjectTypeEnum() {
        return get("subjectTypeEnum");
    }

    public void setSubjectType(GwtSubjectType subjectType) {
        set("subjectType", subjectType.name());
    }

    public String getSubjectId() {
        return get("subjectId");
    }

    public void setSubjectId(String subjectId) {
        set("subjectId", subjectId);
    }
}
