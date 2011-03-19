# Copyright 1999-2006 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: /var/cvsroot/gentoo-x86/media-plugins/vdr-kvdrmon/vdr-kvdrmon-0.6.ebuild,v 1.2 2007/07/10 23:08:59 mr_bones_ Exp $

IUSE=""

inherit vdr-plugin

DESCRIPTION="Helper plugin for Andro-VDR android vdr control app"
HOMEPAGE="http://projects.vdr-developer.org/wiki/kvdrmon"
SRC_URI="http://projects.vdr-developer.org/attachments/download/372/${P}.tgz"
KEYWORDS="~x86 ~amd64"
SLOT="0"
LICENSE="GPL-2"

DEPEND=">=media-video/vdr-1.6.0"

RESTRICT=strip
