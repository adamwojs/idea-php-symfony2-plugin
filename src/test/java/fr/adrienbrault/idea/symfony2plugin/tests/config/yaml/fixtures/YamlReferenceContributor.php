<?php

namespace
{
    define('CONST_FOO', 'CONST_FOO');
}

namespace Foo
{
    class Bar
    {
        const FOO = 'foo';
    }
}

namespace A\B\C {
    class D {}
}