use barley_registry;

DELETE FROM reg_resource WHERE reg_tenant_id = '-1234';

DELETE FROM reg_association WHERE reg_tenant_id = '-1234';

delete From reg_resource_property where reg_property_id in (select reg_id From REG_PROPERTY where reg_name = 'STATUS' AND REG_TENANT_ID=-1234);

delete from REG_PROPERTY where reg_name = 'STATUS' AND REG_TENANT_ID=-1234;


use barley_apimgt;


delete from am_api_lc_event WHERE user_id like 'wso2.system.user%';
delete from am_api_comments WHERE COMMENTED_USER like 'wso2.system.user%';
delete from am_api_ratings;
delete from am_subscription where CREATED_BY like 'wso2.system.user%';
delete from AM_API_URL_MAPPING where API_ID in (select api_id from am_api WHERE API_PROVIDER like 'wso2.system.user%');
delete from am_api WHERE API_PROVIDER like 'wso2.system.user%';

delete from am_application_registration where SUBSCRIBER_ID in (select SUBSCRIBER_ID from am_subscriber where tenant_id = -1234);
delete from AM_APPLICATION_KEY_MAPPING where application_id in (select application_id from am_application where CREATED_BY like 'wso2.system.user%');
delete from IDN_OAUTH_CONSUMER_APPS where username = 'wso2.system.user';
delete from IDN_OAUTH2_ACCESS_TOKEN where TENANT_ID = -1234;

