##/bin/bash

trap 'echo "Something failed.."; exit 1;' ERR


VPPLUGIN_HOME="~/Library/Application Support/VisualParadigm/plugins"
VPPLUGIN_HOME="$HOME/Library/Application Support/VisualParadigm/plugins"

NAME=OraDbCodeGen
OUT=out

HERE=$(dirname "$0")
SRC="$HERE/../$OUT/$NAME"
TRG="$VPPLUGIN_HOME/$NAME"

rm -r "$TRG"
cp -r "$SRC" "$VPPLUGIN_HOME"
exit 0


