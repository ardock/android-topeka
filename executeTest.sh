#!/bin/bash

############  ###########  ##########  #########  ########  #######  ######  #####  ####  ###  ##  #
##
##  EXECUTE TEST SCRIPT
##
############  ###########  ##########  #########  ########  #######  ######  #####  ####  ###  ##  #

# See http://tldp.org/LDP/abs/html/debugging.html
echo Execute Tests:
sh -n ./execute # checks for syntax errors without actually running the script.
# sh ./execute # run the script
# sh -v ./execute # echoes each command before executing it.
# sh -vn ./execute # gives a verbose syntax check.
echo Tests done.
exit 0



# Export environment variables.
if [ -z "${ACIB}" ]; then
    if [ -e "acib" ]; then
        ACIB="acib"
    elif [ -d "scripts" ] &&  [ -e "scripts/acib" ]; then
        ACIB="scripts/acib"
    elif [ -n "${SCRIPTS}" ] && [ -d "${SCRIPTS}" ] && [ -e "${SCRIPTS}/acib" ]; then
        ACIB="${SCRIPTS}/acib"
    else
        echo -e "\033[031m[ ${ACIB} ] script not found.\033[0m" # red error message
        exit 1
    fi
fi

# Execute ACIB script passing to it all the arguments and reading the result code.
"${ACIB}" "$@"
RESULT=$?

# Result: by convention, $? value is 0 on success or an integer in the range 1 - 255 on error.
if [ $RESULT -ne 0 ] ; then
    "${ACIB}" print-format red "[ `basename $0` ] script exited with error code ${RESULT}."
fi
"${ACIB}" print-format reset
exit 0
