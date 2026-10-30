package com.papeleriamatrix.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * La API no tiene una página de inicio propia: quien abre la dirección raíz llega a la documentación.
 */
@Controller
@Hidden
public class InicioController
{
	@GetMapping("/")
	public String inicio()
	{
		return "redirect:/swagger-ui.html";
	}
}
