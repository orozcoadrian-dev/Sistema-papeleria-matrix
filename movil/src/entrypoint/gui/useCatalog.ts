import { computed, onMounted, ref, shallowRef } from 'vue';
import type { TipoDeArticulo } from '../../domain/constants/articulos';
import { coincide, type Articulo } from '../../domain/model/Articulo';
import { mensajeDe, useAplicacion } from './context';

/** Carga, búsqueda y filtro del catálogo: lo comparten la caja y la pantalla de catálogo. */
export function useCatalog(
  alCargar: (articulos: Articulo[]) => void = () => {},
  ajustar: (articulos: Articulo[]) => Articulo[] = (articulos) => articulos,
) {
  const { catalogo } = useAplicacion();
  const articulos = shallowRef<Articulo[]>([]);
  const cargando = ref(true);
  const error = ref('');
  const busqueda = ref('');
  const filtro = ref<TipoDeArticulo | 'todo'>('todo');

  const visibles = computed(() =>
    articulos.value.filter((a) => (filtro.value === 'todo' || a.tipo === filtro.value) && coincide(a, busqueda.value)),
  );

  async function cargar() {
    cargando.value = true;
    error.value = '';
    try {
      articulos.value = ajustar(await catalogo.listar());
      alCargar(articulos.value);
    } catch (fallo) {
      error.value = mensajeDe(fallo);
    } finally {
      cargando.value = false;
    }
  }

  onMounted(cargar);
  return { articulos, visibles, cargando, error, busqueda, filtro, cargar };
}
