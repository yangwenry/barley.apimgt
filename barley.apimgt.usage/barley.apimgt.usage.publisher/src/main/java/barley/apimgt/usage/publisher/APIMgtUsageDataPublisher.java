/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package barley.apimgt.usage.publisher;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.gateway.dto.ExecutionTimePublisherDTO;
import barley.apimgt.usage.publisher.dto.AlertTypeDTO;
import barley.apimgt.usage.publisher.dto.FaultPublisherDTO;
import barley.apimgt.usage.publisher.dto.RequestPublisherDTO;
import barley.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import barley.apimgt.usage.publisher.dto.ThrottlePublisherDTO;

public interface APIMgtUsageDataPublisher {

    public void init();

    public void publishEvent(RequestPublisherDTO requestPublisherDTO);

    public void publishEvent(ResponsePublisherDTO responsePublisherDTO);

    public void publishEvent(FaultPublisherDTO faultPublisherDTO);

    public void publishEvent(ThrottlePublisherDTO throttlePublisherDTO);

    public void publishEvent(ExecutionTimePublisherDTO executionTimePublisherDTO);

    public void publishEvent(AlertTypeDTO alertTypeDTO) throws APIManagementException;

}
