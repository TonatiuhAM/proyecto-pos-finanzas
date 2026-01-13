import { useState, useEffect } from 'react';
import {
  Users,
  Search,
  Briefcase,
  Truck,
  UserCircle,
  Plus,
  CheckCircle2,
  XCircle,
  ToggleLeft,
  ToggleRight,
  Mail,
  Phone,
  BadgeCheck
} from 'lucide-react';
import SidebarNavigation from './SidebarNavigation';

// --- IMPORTS DE LÓGICA EXISTENTE ---
import { empleadoService } from '../services/empleadoService';
import { personaService } from '../services/personaService';
import { TipoCategoriaPersona } from '../types';
import { useToast } from '../hooks/useToast';
import './GestionPersonas.css';

// --- TIPOS DEL BACKEND ---
interface Empleado {
  id: string;
  nombre: string;
  telefono: string;
  rolNombre: string;
  estadoNombre: string;
}

interface PersonaResponse {
  id: string;
  nombreCompleto: string;
  rfc?: string;
  email?: string;
  telefono?: string;
  nombreEstado: string;
}

// Tipo Unión para la tabla
type TableItem = Empleado | PersonaResponse;

// --- COMPONENTES VISUALES AUXILIARES ---

const StatusBadge = ({ status }: { status: string }) => {
  const isActive = status?.toLowerCase() === 'activo';
  return (
    <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold capitalize shadow-sm ${isActive
      ? 'bg-green-100 text-green-700 border border-green-200'
      : 'bg-red-50 text-red-600 border border-red-100'
      }`}>
      {isActive ? <CheckCircle2 className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
      {isActive ? 'Activo' : 'Inactivo'}
    </span>
  );
};

const ActionToggle = ({
  status,
  onToggle,
  loading = false
}: {
  status: string;
  onToggle: () => void;
  loading?: boolean;
}) => {
  const isActive = status?.toLowerCase() === 'activo';
  return (
    <div className="flex items-center justify-center gap-2 group cursor-pointer" onClick={loading ? undefined : onToggle}>
      <span className={`text-xs font-medium transition-colors ${isActive ? 'text-gray-400' : 'text-gray-500 font-bold'}`}>Off</span>
      <button
        disabled={loading}
        className={`relative transition-all duration-300 ease-in-out focus:outline-none transform active:scale-95 ${isActive ? 'text-orange-500 drop-shadow-md' : 'text-gray-300'
          } ${loading ? 'opacity-50 cursor-wait' : ''}`}
      >
        {isActive ? (
          <ToggleRight className="w-9 h-9 fill-current" />
        ) : (
          <ToggleLeft className="w-9 h-9 fill-current hover:text-gray-400" />
        )}
      </button>
      <span className={`text-xs font-medium transition-colors ${isActive ? 'text-orange-600 font-bold' : 'text-gray-400'}`}>On</span>
    </div>
  );
};

// --- COMPONENTE DE TABLA PRINCIPAL ---
interface ManagementTableProps {
  title: string;
  subtitle: string;
  data: TableItem[];
  columns: any[];
  type: string;
  icon: any;
  currentTab: string;
  onTabChange: (id: string) => void;
  loading: boolean;
}

const ManagementTable = ({
  title,
  subtitle,
  data,
  columns,
  type,
  icon: Icon,
  currentTab,
  onTabChange,
  loading
}: ManagementTableProps) => {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredData = data.filter((item: any) => {
    const name = (item.nombre || item.nombreCompleto || '').toLowerCase();
    const email = (item.email || '').toLowerCase();
    const search = searchTerm.toLowerCase();
    return name.includes(search) || email.includes(search);
  });

  const tabs = [
    { id: 'employees', label: 'Empleados', icon: Briefcase },
    { id: 'suppliers', label: 'Proveedores', icon: Truck },
    { id: 'customers', label: 'Clientes', icon: Users },
  ];

  return (
    <div className="flex flex-col h-full animate-in fade-in slide-in-from-bottom-2 duration-500">

      {/* 1. Encabezado */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-4 mb-6">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <div className="p-2 bg-orange-100 rounded-xl text-orange-600">
              <Icon className="w-6 h-6" />
            </div>
            <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">{title}</h2>
          </div>
          <p className="text-gray-500 font-medium ml-1 mb-4">{subtitle}</p>

          {/* TABS */}
          <div className="flex flex-wrap gap-2 mt-2">
            {tabs.map((tab) => {
              const isActive = currentTab === tab.id;
              const TabIcon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => onTabChange(tab.id)}
                  className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold transition-all duration-200 border ${isActive
                    ? 'bg-gray-800 text-white border-gray-800 shadow-md transform scale-105'
                    : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50 hover:border-gray-300'
                    }`}
                >
                  <TabIcon className={`w-4 h-4 ${isActive ? 'text-orange-400' : 'text-gray-400'}`} />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        <div className="flex gap-3 w-full md:w-auto">
          <button className="flex-1 md:flex-none flex items-center justify-center gap-2 bg-orange-600 hover:bg-orange-700 text-white px-5 py-3 rounded-xl shadow-lg shadow-orange-200 transition-all active:scale-95 font-bold h-fit">
            <Plus className="w-5 h-5" />
            <span>Nuevo {type}</span>
          </button>
        </div>
      </div>

      {/* 2. Barra de Búsqueda */}
      <div className="bg-white p-4 rounded-[1.5rem] shadow-sm border border-gray-100 mb-6 flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="relative w-full">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder={`Buscar ${title.toLowerCase()}...`}
            className="w-full pl-11 pr-4 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-orange-200 focus:bg-white transition-all text-sm font-medium text-gray-700 placeholder-gray-400 outline-none"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* 3. La Tabla */}
      <div className="bg-white rounded-[2rem] shadow-md border border-gray-100 overflow-hidden flex-1 flex flex-col mb-6">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse min-w-[900px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50/80 text-gray-400 text-xs uppercase tracking-wider">
                {columns.map((col: any, idx: number) => (
                  <th key={idx} className={`p-6 font-bold ${col.align || 'text-left'}`}>
                    {col.header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {loading ? (
                <tr>
                  <td colSpan={columns.length} className="p-10 text-center">
                    <div className="flex flex-col justify-center items-center gap-2 text-gray-400">
                      <div className="w-8 h-8 border-4 border-orange-200 border-t-orange-500 rounded-full animate-spin"></div>
                      <span className="text-sm font-medium">Cargando datos...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredData.length > 0 ? (
                filteredData.map((item: any, index: number) => (
                  <tr key={item.id || index} className="hover:bg-orange-50/40 transition-colors group">
                    {columns.map((col: any, colIdx: number) => (
                      <td key={colIdx} className={`p-5 ${col.className || ''}`}>
                        {col.render ? col.render(item) : (
                          <span className="font-medium text-gray-700">{item[col.key]}</span>
                        )}
                      </td>
                    ))}
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={columns.length} className="p-10 text-center">
                    <div className="flex flex-col items-center justify-center text-gray-400">
                      <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4">
                        <Search className="w-8 h-8 text-gray-300" />
                      </div>
                      <p>No se encontraron resultados para "{searchTerm}"</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

// --- COMPONENTE PRINCIPAL ---
const GestionPersonas = ({ onNavigate }: { onNavigate: (section: string) => void }) => {
  const [activeTab, setActiveTab] = useState('employees');
  const [empleados, setEmpleados] = useState<Empleado[]>([]);
  const [proveedores, setProveedores] = useState<PersonaResponse[]>([]);
  const [clientes, setClientes] = useState<PersonaResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [toggleLoading, setToggleLoading] = useState<string | null>(null);

  const { showSuccess, showError } = useToast();

  const cargarDatos = async (tab: string) => {
    setLoading(true);
    try {
      switch (tab) {
        case 'employees':
          const empleadosData = await empleadoService.obtenerEmpleados();
          setEmpleados(empleadosData);
          break;
        case 'suppliers':
          const proveedoresData = await personaService.obtenerPersonasPorCategoria(TipoCategoriaPersona.PROVEEDOR);
          setProveedores(proveedoresData);
          break;
        case 'customers':
          const clientesData = await personaService.obtenerPersonasPorCategoria(TipoCategoriaPersona.CLIENTE);
          setClientes(clientesData);
          break;
      }
    } catch (err) {
      console.error(err);
      showError(`Error al cargar datos`);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (id: string, currentStatus: string, type: string) => {
    setToggleLoading(id);
    try {
      const newStatus = currentStatus === 'Activo' ? 'Inactivo' : 'Activo';

      if (type === 'empleado') {
        await empleadoService.cambiarEstadoEmpleado(id, newStatus);
        await cargarDatos('employees');
        showSuccess(`Estado actualizado correctamente`);
      } else {
        if (newStatus === 'Activo') {
          await personaService.activarPersona(id);
        } else {
          await personaService.desactivarPersona(id);
        }
        if (type === 'proveedor') await cargarDatos('suppliers');
        else await cargarDatos('customers');

        showSuccess(`Estado actualizado correctamente`);
      }
    } catch (err) {
      console.error(err);
      showError(`Error al cambiar estado`);
    } finally {
      setToggleLoading(null);
    }
  };

  useEffect(() => {
    cargarDatos(activeTab);
  }, [activeTab]);

  const employeeColumns = [
    {
      header: 'Nombre del Empleado',
      key: 'nombre',
      render: (item: Empleado) => (
        <div className="flex items-center gap-4">
          <div className="w-11 h-11 rounded-full bg-gradient-to-br from-gray-100 to-gray-200 border-2 border-white shadow-sm flex items-center justify-center text-gray-600 font-bold text-sm">
            {(item.nombre || '?').charAt(0)}
          </div>
          <div>
            <span className="block font-bold text-gray-800 text-base">{item.nombre}</span>
            <span className="text-xs text-orange-500 font-medium">ID: ...{(item.id || '').slice(-4)}</span>
          </div>
        </div>
      )
    },
    {
      header: 'Teléfono',
      key: 'telefono',
      render: (item: Empleado) => (
        <div className="flex items-center gap-2 text-gray-600 font-medium">
          <Phone className="w-4 h-4 text-gray-400" />
          {item.telefono || 'Sin teléfono'}
        </div>
      )
    },
    {
      header: 'Rol / Puesto',
      key: 'rolNombre',
      render: (item: Empleado) => (
        <div className="flex items-center gap-2">
          <BadgeCheck className="w-4 h-4 text-blue-500" />
          <span className="font-semibold text-gray-700">{item.rolNombre}</span>
        </div>
      )
    },
    {
      header: 'Estado Actual',
      key: 'estadoNombre',
      render: (item: Empleado) => <StatusBadge status={item.estadoNombre} />
    },
    {
      header: 'Acciones',
      key: 'actions',
      align: 'text-center',
      render: (item: Empleado) => (
        <ActionToggle
          status={item.estadoNombre}
          loading={toggleLoading === item.id}
          onToggle={() => handleToggleStatus(item.id, item.estadoNombre, 'empleado')}
        />
      )
    }
  ];

  const supplierColumns = [
    {
      header: 'Nombre / Empresa',
      key: 'nombreCompleto',
      render: (item: PersonaResponse) => (
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 border border-purple-100 shadow-sm">
            <Truck className="w-6 h-6" />
          </div>
          <div>
            <span className="block font-bold text-gray-800">{item.nombreCompleto}</span>
            <span className="text-xs text-gray-400">Proveedor</span>
          </div>
        </div>
      )
    },
    {
      header: 'RFC',
      key: 'rfc',
      render: (item: PersonaResponse) => (
        <span className="font-mono text-xs font-bold text-gray-500 bg-gray-100 px-2 py-1 rounded border border-gray-200">
          {item.rfc || 'N/A'}
        </span>
      )
    },
    {
      header: 'Email',
      key: 'email',
      render: (item: PersonaResponse) => (
        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Mail className="w-4 h-4 text-gray-400" />
          {item.email || 'Sin email'}
        </div>
      )
    },
    {
      header: 'Estado',
      key: 'nombreEstado',
      render: (item: PersonaResponse) => <StatusBadge status={item.nombreEstado} />
    },
    {
      header: 'Acciones',
      align: 'text-center',
      render: (item: PersonaResponse) => (
        <ActionToggle
          status={item.nombreEstado}
          loading={toggleLoading === item.id}
          onToggle={() => handleToggleStatus(item.id, item.nombreEstado, 'proveedor')}
        />
      )
    }
  ];

  const customerColumns = [
    {
      header: 'Cliente',
      key: 'nombreCompleto',
      render: (item: PersonaResponse) => (
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-full bg-yellow-50 flex items-center justify-center text-yellow-600 border border-yellow-100 shadow-sm">
            <UserCircle className="w-6 h-6" />
          </div>
          <div>
            <span className="block font-bold text-gray-800">{item.nombreCompleto}</span>
            <span className="text-xs text-gray-400">Cliente</span>
          </div>
        </div>
      )
    },
    {
      header: 'RFC',
      key: 'rfc',
      render: (item: PersonaResponse) => (
        <span className="font-mono text-xs font-bold text-gray-500 bg-gray-100 px-2 py-1 rounded border border-gray-200">
          {item.rfc || 'N/A'}
        </span>
      )
    },
    {
      header: 'Email',
      key: 'email',
      render: (item: PersonaResponse) => (
        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Mail className="w-4 h-4 text-gray-400" />
          {item.email || 'Sin email'}
        </div>
      )
    },
    {
      header: 'Estado',
      key: 'nombreEstado',
      render: (item: PersonaResponse) => <StatusBadge status={item.nombreEstado} />
    },
    {
      header: 'Acciones',
      align: 'text-center',
      render: (item: PersonaResponse) => (
        <ActionToggle
          status={item.nombreEstado}
          loading={toggleLoading === item.id}
          onToggle={() => handleToggleStatus(item.id, item.nombreEstado, 'cliente')}
        />
      )
    }
  ];

  return (
    <div className="gestion-personas-container">
      <SidebarNavigation activeSection="personal" onNavigate={onNavigate} />

      <main className="gestion-personas-main">
        {activeTab === 'employees' && (
          <ManagementTable
            title="Lista de Empleados"
            subtitle="Administra el acceso y roles de tu personal."
            type="Empleado"
            icon={Briefcase}
            data={empleados}
            columns={employeeColumns}
            currentTab={activeTab}
            onTabChange={setActiveTab}
            loading={loading}
          />
        )}

        {activeTab === 'suppliers' && (
          <ManagementTable
            title="Lista de Proveedores"
            subtitle="Gestiona la información de tus socios comerciales."
            type="Proveedor"
            icon={Truck}
            data={proveedores}
            columns={supplierColumns}
            currentTab={activeTab}
            onTabChange={setActiveTab}
            loading={loading}
          />
        )}

        {activeTab === 'customers' && (
          <ManagementTable
            title="Lista de Clientes"
            subtitle="Base de datos de tus clientes facturables."
            type="Cliente"
            icon={Users}
            data={clientes}
            columns={customerColumns}
            currentTab={activeTab}
            onTabChange={setActiveTab}
            loading={loading}
          />
        )}
      </main>
    </div>
  );
};

export default GestionPersonas;
