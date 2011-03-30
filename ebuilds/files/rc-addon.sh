plugin_pre_vdr_start() {

	if [ "${VDRMANAGER_PORT:=6420}" != 6420 ]; then
		add_plugin_param "-p ${VDRMANAGER_PORT}"
	fi

	if [ -n "${VDRMANAGER_PASSWORD}" ]; then
		add_plugin_param "-P ${VDRMANAGER_PASSWORD}"
	fi
}
