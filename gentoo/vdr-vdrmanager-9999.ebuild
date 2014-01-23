# Copyright 1999-2013 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: /var/cvsroot/gentoo-x86/media-plugins/vdr-vdrmanager/vdr-vdrmanager-0.10.ebuild,v 1.1 2013/12/27 17:18:28 hd_brummy Exp $

EAPI="5"

inherit vdr-plugin-2 ssl-cert git-2

EGIT_REPO_URI="git://projects.vdr-developer.org/vdr-manager.git"

DESCRIPTION="VDR Plugin: allows remote programming VDR using VDR-Manager running on Android devices"
HOMEPAGE="http://projects.vdr-developer.org/projects/vdr-manager/wiki"

KEYWORDS="~x86 ~amd64"
SLOT="0"
LICENSE="GPL-2"

IUSE="-stream +ssl +zlib -gzip"

DEPEND=">=media-video/vdr-2
        ssl? ( dev-libs/openssl )
        zlib? ( sys-libs/zlib )"
RDEPEND="stream? ( media-plugins/vdr-streamdev[server] )
        gzip? ( app-arch/gzip )
        $DEPEND"

VDRMANAGER_SSL_KEY_DIR="/etc/vdr/plugins/vdrmanager"
VDRMANAGER_SSL_KEY_FILE="${VDRMANAGER_SSL_KEY_DIR}/vdrmanager"

S="${WORKDIR}/${P}"

make_vdrmanager_cert() {
    SSL_COUNTRY="${SSL_COUNTRY:-}"
    SSL_STATE="${SSL_STATE:-Unknown}"
    SSL_LOCALITY="${VDRMANAGER_SSL_LOCALITY:-Unkown}"
    SSL_ORGANIZATION="${VDRMNAGER_SSL_ORGANIZATION:-VDR-Manager Plugin}"
    SSL_UNIT="${VDRMANAGER_SSL_UNIT:-VDR Server}"
    SSL_COMMONNAME="${VDRMANAGER_SSL_COMMONNAME:-`hostname -f`}"
    SSL_EMAIL="${VDRMANAGER_SSL_EMAIL:-Unknown}"
	SSL_BITS="${VDRMANAGER_SSL_BITS:-1024}"
	SSL_DAYS="${VDRMANAGER_SSL_DAYS:-720}"

	rm -f ${ROOT}${VDRMANAGER_SSL_KEY_FILE}.*

	install_cert ${VDRMANAGER_SSL_KEY_FILE}

	rm -f ${ROOT}${VDRMANAGER_SSL_KEY_FILE}.{crt,csr,key}
	chown vdr:vdr "${ROOT}${VDRMANAGER_SSL_KEY_FILE}.pem"
	chmod 0600 "${ROOT}${VDRMANAGER_SSL_KEY_FILE}.pem"
}

src_unpack() {
	git-2_src_unpack
	S=${WORKDIR}/${P}/${PN}
}

src_prepare() {

    use ssl  || BUILD_PARAMS="$BUILD_PARAMS VDRMANAGER_USE_SSL=0"
    use gzip || BUILD_PARAMS="$BUILD_PARAMS VDRMANAGER_USE_GZIP=0"
    use zlib || BUILD_PARAMS="$BUILD_PARAMS VDRMANAGER_USE_ZLIB=0"

    vdr-plugin-2_src_prepare
}

pkg_postinst() {
	vdr-plugin-2_pkg_postinst

	einfo "Add a password to /etc/conf.d/vdr.vdrmanager"

	if use ssl; then
		if path_exists -a "${ROOT}${VDRMANAGER_SSL_KEY_FILE}.pem"; then
			einfo "found an existing SSL cert, to create a new SSL cert, run:\n"
			einfo "emerge --config ${PN}"
		else
			einfo "No SSL cert found, creating a default one now"
			make_vdrmanager_cert
		fi
		einfo
	fi

	if use gzip; then
		einfo 'The plugin was installed with USE flag "gzip" set.'
		einfo 'You must install app-arch/gzip to use the gzip'
		einfo 'compression method.'
	fi
}

pkg_config() {
	make_vdrmanager_cert
}

