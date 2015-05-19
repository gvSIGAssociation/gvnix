/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 *
* This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.json;

import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson {@link SimpleModule} which registers
 * {@link ConversionServiceBeanSerializerModifier}
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
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
