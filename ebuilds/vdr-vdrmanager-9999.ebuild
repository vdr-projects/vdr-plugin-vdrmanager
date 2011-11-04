# Copyright 1999-2011 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: 

EAPI=2

inherit vdr-plugin git-2

EGIT_REPO_URI="git://projects.vdr-developer.org/vdr-manager.git"

DESCRIPTION="Helper plugin for the VDR-Manager android vdr control app"
HOMEPAGE="http://projects.vdr-developer.org/wiki/androvdr"
SLOT="0"
LICENSE="GPL-2"
KEYWORDS="~x86 ~amd64"
IUSE=""

DEPEND=">=media-video/vdr-1.6.0"

S=${WORKDIR}/${P}

RESTRICT=strip

src_unpack() {
	git-2_src_unpack
	S=${WORKDIR}/${P}/${PN}
}

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

