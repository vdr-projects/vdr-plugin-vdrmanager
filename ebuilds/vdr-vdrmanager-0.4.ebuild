# Copyright 1999-2006 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2


IUSE=""

inherit vdr-plugin

DESCRIPTION="Helper plugin for the VDR-Manager android vdr control app"
HOMEPAGE="http://projects.vdr-developer.org/wiki/androvdr"
SRC_URI="http://projects.vdr-developer.org/attachments/download/787/${P}.tar.gz"
KEYWORDS="~x86 ~amd64"
SLOT="0"
LICENSE="GPL-2"

DEPEND=">=media-video/vdr-1.6.0"

S="${WORKDIR}/${PN}"

RESTRICT=strip

src_install() {
	vdr-plugin_src_install

	insinto /etc/conf.d
	newins examples/vdr.vdrmanager vdr.vdrmanager
	fperms 600 /etc/conf.d/vdr.vdrmanager
}

pkg_postinst() {
	ewarn
	ewarn "You should change the default password \"change\" in /etc/conf.d/vdr.vdrmanager"
	ewarn
	ewarn "If you want to stream from your VDR to your android device, you need to install"
	ewarn "vdr-streamdev with USE=\"server\" set"
	ewarn
}

