"""
Test básico para verificar configuración de pytest
"""
import pytest


def test_configuracion_pytest():
    """Verifica que pytest está configurado correctamente"""
    assert True


def test_operaciones_basicas():
    """Verifica operaciones matemáticas básicas"""
    assert 2 + 2 == 4
    assert 10 - 5 == 5
    assert 3 * 4 == 12


def test_strings():
    """Verifica operaciones con strings"""
    assert "Hello" + " " + "World" == "Hello World"
    assert "test".upper() == "TEST"


@pytest.mark.parametrize("input,expected", [
    (1, 2),
    (2, 4),
    (3, 6),
    (4, 8),
])
def test_multiplicacion_por_dos(input, expected):
    """Test parametrizado para verificar multiplicación"""
    assert input * 2 == expected
