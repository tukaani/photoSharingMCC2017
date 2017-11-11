#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved

from app import app

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8080, debug=True)
