/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gvnix.web.json;

import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson {@link SimpleModule} which registers
 * {@link ConversionServiceBeanSerializerModifier}
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since TODO: Class version
 */
public class ConversionServiceModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    private ConversionService conversionService = null;

    public ConversionServiceModule(ConversionService conversionService) {
        super();
        this.conversionService = conversionService;
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanSerializerModifier(new ConversionServiceBeanSerializerModifier(
                conversionService));
    }

}
