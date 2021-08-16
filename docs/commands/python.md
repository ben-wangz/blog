### I need a random string(including digits)

```shell
python -c "import random;import string;print(''.join(random.sample(string.ascii_letters + string.digits, 8)))"
```