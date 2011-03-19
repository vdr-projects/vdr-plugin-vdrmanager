plugin_pre_vdr_start() {

	if [ "${ANDROVDR_PORT:=6420}" != 6420 ]; then
		add_plugin_param "-p ${ANDROVDR_PORT}"
	fi

	if [ -n "${ANDROVDR_PASSWORD}" ]; then
		add_plugin_param "-P ${ANDROVDR_PASSWORD}"
	fi
}
