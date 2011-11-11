#
# Regular cron jobs for the vdr-plugin-vdr-vdrmanager package
#
0 4	* * *	root	[ -x /usr/bin/vdr-plugin-vdr-vdrmanager_maintenance ] && /usr/bin/vdr-plugin-vdr-vdrmanager_maintenance
