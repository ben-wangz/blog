#include "include/geekmath.hpp"
#include <iostream>

int main() {
    geekmath::Simple simple;
    std::cout << "hello world: "
        << "1 + 2 = " << simple.add(1, 2) << std::endl;
    return 0;
}
