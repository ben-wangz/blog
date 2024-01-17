# python

## I need a random string(including digits)

```shell
python -c "import random;import string;print(''.join(random.sample(string.ascii_letters + string.digits, 8)))"
```

## create a virtual env

```shell
python3 -m venv virtualenv
```

## install requirements

```shell
pip3 install -i http://mirrors.aliyun.com/pypi/simple -r requirements.txt --trusted-host mirrors.aliyun.com
```