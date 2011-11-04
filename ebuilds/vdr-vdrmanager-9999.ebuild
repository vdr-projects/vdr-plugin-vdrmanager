# Copyright 1999-2006 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2

IUSE=""

inherit vdr-plugin git-2

DESCRIPTION="Helper plugin for the VDR-Manager android vdr control app"
HOMEPAGE="http://projects.vdr-developer.org/projects/vdr-manager/wiki"
#SRC_URI="http://projects.vdr-developer.org/attachments/download/574/${P}.tgz"
S="${WORKDIR}/vdr-vdrmanager-0.2"
EGIT_REPO_URI="git://projects.vdr-developer.org/vdr-manager.git"

KEYWORDS="~x86 ~amd64"
SLOT="0"
LICENSE="GPL-2"

DEPEND=">=media-video/vdr-1.6.0"

RESTRICT=strip
