/**
 * Prueba unitaria local de ejemplo para validar logica independiente del framework de Android.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    /**
     * Metodo principal que ejecuta la operacion: addition_isCorrect.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}