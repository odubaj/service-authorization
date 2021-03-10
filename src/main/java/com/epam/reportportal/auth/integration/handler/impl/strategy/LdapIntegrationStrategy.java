/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.auth.integration.handler.impl.strategy;

import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.parameter.LdapParameter;
import com.epam.reportportal.auth.integration.parameter.ParameterUtils;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.integration.auth.UpdateAuthRQ;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class LdapIntegrationStrategy extends AuthIntegrationStrategy {

	private final BasicTextEncryptor encryptor;

	@Autowired
	public LdapIntegrationStrategy(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			BasicTextEncryptor encryptor) {
		super(integrationTypeRepository, integrationRepository, AuthIntegrationType.LDAP);
		this.encryptor = encryptor;
	}

	@Override
	protected void validateRequest(UpdateAuthRQ request) {
		ParameterUtils.validateLdapRequest(request);
		LdapParameter.MANAGER_PASSWORD.getParameter(request)
				.ifPresent(it -> request.getIntegrationParams()
						.put(LdapParameter.MANAGER_PASSWORD.getParameterName(), encryptor.encrypt(it)));
	}

	@Override
	protected void validateDuplicate(Integration integration, UpdateAuthRQ request) {
		getIntegrationRepository().findByNameAndTypeIdAndProjectIdIsNull(AuthIntegrationType.LDAP.getName(), integration.getType().getId())
				.ifPresent(it -> BusinessRule.expect(it.getId(), id -> id.equals(integration.getId()))
						.verify(ErrorType.INTEGRATION_ALREADY_EXISTS, integration.getName()));
	}
}
