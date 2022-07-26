import os
from datetime import datetime


def main():
    os.system('heroku pg:backups:capture -a cashew-jda')
    os.system('heroku pg:backups:download -a cashew-jda')
    dump_name = 'backups/' + datetime.strftime(datetime.now(),
                                               '%m%d%Y-%H%M%S') + '.dump'
    if not os.path.exists('backups'):
        os.makedirs('backups')
    os.rename('latest.dump', dump_name)


if __name__ == '__main__':
    main()
