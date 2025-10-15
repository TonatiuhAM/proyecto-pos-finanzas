import React, { useState, useEffect } from 'react';
import { 
  TrendingUp, 
  Package, 
  AlertTriangle, 
  Clock, 
  Target,
  RefreshCw,
  Brain,
  Activity
} from 'lucide-react';
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  CircularProgress,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Checkbox,
  Chip,
  Box,
  Typography,
  type SelectChangeEvent,
} from '@mui/material';
import { mlService, useMLPredictions } from '../services/mlService';

interface ModalPrediccionesProps {
  isOpen: boolean;
  onClose: () => void;
  onCreatePurchaseOrder?: (productos: any[]) => void;
}

const ModalPredicciones: React.FC<ModalPrediccionesProps> = ({
  isOpen,
  onClose,
  onCreatePurchaseOrder
}) => {
  const [selectedProducts, setSelectedProducts] = useState<Set<string>>(new Set());
  const [filterCategory, setFilterCategory] = useState<string>('');
  const [sortBy, setSortBy] = useState<'prioridad' | 'cantidad' | 'confianza'>('prioridad');
  const [formattedPredictions, setFormattedPredictions] = useState<Array<{
    id: string;
    nombre: string;
    categoria: string;
    cantidadSugerida: number;
    prioridad: 'Alta' | 'Media' | 'Baja';
    prioridadScore: number;
    stockActual: number;
    diasStock: number | null;
    confianza: string;
    recomendacion: string;
  }>>([]);
  
  const {
    predictions,
    loading,
    error,
    isMLAvailable,
    loadPredictions
  } = useMLPredictions();

  // Cargar predicciones cuando se abre el modal
  useEffect(() => {
    if (isOpen && isMLAvailable) {
      loadPredictions();
    }
  }, [isOpen, isMLAvailable]);

  // Formatear predicciones cuando cambian
  useEffect(() => {
    const formatPredictions = async () => {
      if (predictions.length > 0) {
        const formatted = await mlService.formatPredictionsForDisplay({
          predicciones: predictions,
          timestamp: new Date().toISOString(),
          modelo_version: '1.0.0'
        });
        setFormattedPredictions(formatted);
      } else {
        setFormattedPredictions([]);
      }
    };

    formatPredictions();
  }, [predictions]);

  if (!isOpen) return null;

  // Filtrar y ordenar predicciones
  const filteredAndSorted = formattedPredictions
    .filter(pred => !filterCategory || pred.categoria.includes(filterCategory))
    .sort((a, b) => {
      switch (sortBy) {
        case 'prioridad':
          return b.prioridadScore - a.prioridadScore;
        case 'cantidad':
          return b.cantidadSugerida - a.cantidadSugerida;
        case 'confianza':
          return parseFloat(b.confianza) - parseFloat(a.confianza);
        default:
          return 0;
      }
    });

  const handleSelectProduct = (productId: string) => {
    const newSelected = new Set(selectedProducts);
    if (newSelected.has(productId)) {
      newSelected.delete(productId);
    } else {
      newSelected.add(productId);
    }
    setSelectedProducts(newSelected);
  };

  const handleSelectChange = (e: SelectChangeEvent<string>) => {
    const { name, value } = e.target;
    if (name === 'filterCategory') {
      setFilterCategory(value);
    } else if (name === 'sortBy') {
      setSortBy(value as 'prioridad' | 'cantidad' | 'confianza');
    }
  };

  const handleSelectAll = () => {
    if (selectedProducts.size === filteredAndSorted.length) {
      setSelectedProducts(new Set());
    } else {
      setSelectedProducts(new Set(filteredAndSorted.map(p => p.id)));
    }
  };

  const handleCreatePurchaseOrder = () => {
    const selectedPredictions = filteredAndSorted.filter(p => selectedProducts.has(p.id));
    if (onCreatePurchaseOrder) {
      onCreatePurchaseOrder(selectedPredictions);
    }
    handleClose();
  };

  const handleClose = () => {
    setSelectedProducts(new Set());
    setFilterCategory('');
    setSortBy('prioridad');
    onClose();
  };

  const getPriorityIcon = (prioridad: string) => {
    switch (prioridad) {
      case 'Alta': return <AlertTriangle size={16} />;
      case 'Media': return <Clock size={16} />;
      case 'Baja': return <Target size={16} />;
      default: return <Package size={16} />;
    }
  };

  return (
    <Dialog open={isOpen} onClose={handleClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={2}>
          <Box 
            sx={{
              backgroundColor: 'primary.main',
              color: 'white',
              borderRadius: 2,
              p: 1,
              display: 'flex',
              alignItems: 'center'
            }}
          >
            <Brain size={24} />
          </Box>
          <Box>
            <Typography variant="h6" component="div">
              Predicciones de Compra ML
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sistema inteligente de recomendaciones de abastecimiento
            </Typography>
          </Box>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {/* Estado del servicio ML */}
        {!isMLAvailable && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            <Box display="flex" alignItems="center" gap={1}>
              <Activity size={20} />
              El servicio de Machine Learning no está disponible. Las predicciones podrían estar limitadas.
            </Box>
          </Alert>
        )}

        {/* Controles */}
        <Box sx={{ mb: 3, p: 2, backgroundColor: 'grey.50', borderRadius: 1 }}>
          <Box display="flex" flexWrap="wrap" alignItems="center" justifyContent="space-between" gap={2}>
            <Box display="flex" alignItems="center" gap={2}>
              <FormControl size="small" sx={{ minWidth: 200 }}>
                <InputLabel>Filtrar por categoría</InputLabel>
                <Select
                  name="filterCategory"
                  value={filterCategory}
                  label="Filtrar por categoría"
                  onChange={handleSelectChange}
                >
                  <MenuItem value="">Todas las categorías</MenuItem>
                  {Array.from(new Set(formattedPredictions.map(p => p.categoria))).map(cat => (
                    <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                  ))}
                </Select>
              </FormControl>

              <FormControl size="small" sx={{ minWidth: 150 }}>
                <InputLabel>Ordenar por</InputLabel>
                <Select
                  name="sortBy"
                  value={sortBy}
                  label="Ordenar por"
                  onChange={handleSelectChange}
                >
                  <MenuItem value="prioridad">Prioridad</MenuItem>
                  <MenuItem value="cantidad">Cantidad sugerida</MenuItem>
                  <MenuItem value="confianza">Confianza</MenuItem>
                </Select>
              </FormControl>
            </Box>

            <Box display="flex" alignItems="center" gap={1}>
              <Button
                onClick={() => loadPredictions()}
                disabled={loading}
                variant="contained"
                size="small"
                startIcon={loading ? <CircularProgress size={16} /> : <RefreshCw size={16} />}
              >
                Actualizar
              </Button>

              <Button
                onClick={handleSelectAll}
                variant="outlined"
                size="small"
              >
                {selectedProducts.size === filteredAndSorted.length ? 'Deseleccionar todo' : 'Seleccionar todo'}
              </Button>
            </Box>
          </Box>
        </Box>

        {/* Contenido principal */}
        <Box sx={{ maxHeight: '60vh', overflow: 'auto' }}>
          {loading ? (
            <Box display="flex" justifyContent="center" alignItems="center" py={6}>
              <Box display="flex" alignItems="center" gap={2}>
                <CircularProgress />
                <Typography>Generando predicciones...</Typography>
              </Box>
            </Box>
          ) : error ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              <Box>
                <Typography variant="subtitle2" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <AlertTriangle size={20} />
                  Error al obtener predicciones
                </Typography>
                <Typography variant="body2">{error}</Typography>
                <Button
                  onClick={() => loadPredictions()}
                  variant="contained"
                  color="error"
                  size="small"
                  sx={{ mt: 1 }}
                >
                  Reintentar
                </Button>
              </Box>
            </Alert>
          ) : filteredAndSorted.length === 0 ? (
            <Box display="flex" flexDirection="column" alignItems="center" py={6}>
              <Package size={48} style={{ opacity: 0.5, marginBottom: 16 }} />
              <Typography color="text.secondary">No se encontraron predicciones para mostrar</Typography>
            </Box>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={selectedProducts.size === filteredAndSorted.length && filteredAndSorted.length > 0}
                        onChange={handleSelectAll}
                      />
                    </TableCell>
                    <TableCell>Producto</TableCell>
                    <TableCell align="center">Cantidad Sugerida</TableCell>
                    <TableCell align="center">Prioridad</TableCell>
                    <TableCell align="center">Stock Actual</TableCell>
                    <TableCell align="center">Días Stock</TableCell>
                    <TableCell align="center">Confianza</TableCell>
                    <TableCell>Recomendación</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredAndSorted.map((prediccion) => (
                    <TableRow key={prediccion.id} hover>
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedProducts.has(prediccion.id)}
                          onChange={() => handleSelectProduct(prediccion.id)}
                        />
                      </TableCell>
                      <TableCell>
                        <Box>
                          <Typography variant="body2" fontWeight="medium">
                            {prediccion.nombre}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {prediccion.categoria}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Box display="flex" alignItems="center" justifyContent="center" gap={0.5}>
                          <TrendingUp size={16} color="#1976d2" />
                          <Typography variant="body2" fontWeight="medium">
                            {prediccion.cantidadSugerida}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          size="small"
                          icon={getPriorityIcon(prediccion.prioridad)}
                          label={prediccion.prioridad}
                          color={
                            prediccion.prioridad === 'Alta' ? 'error' :
                            prediccion.prioridad === 'Media' ? 'warning' : 'success'
                          }
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Typography 
                          variant="body2"
                          color={prediccion.stockActual === 0 ? 'error.main' : 'text.primary'}
                          fontWeight={prediccion.stockActual === 0 ? 'medium' : 'normal'}
                        >
                          {prediccion.stockActual} pz
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography 
                          variant="body2"
                          color={
                            prediccion.diasStock === null ? 'text.secondary' :
                            prediccion.diasStock < 7 ? 'error.main' :
                            prediccion.diasStock < 14 ? 'warning.main' : 'text.primary'
                          }
                          fontWeight={prediccion.diasStock !== null && prediccion.diasStock < 7 ? 'medium' : 'normal'}
                        >
                          {prediccion.diasStock !== null ? `${prediccion.diasStock} días` : 'N/A'}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2" fontWeight="medium">
                          {prediccion.confianza}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {prediccion.recomendacion}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 2, backgroundColor: 'grey.50' }}>
        <Box display="flex" justifyContent="space-between" width="100%" alignItems="center">
          <Box>
            {selectedProducts.size > 0 && (
              <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box 
                  sx={{ 
                    width: 12, 
                    height: 12, 
                    backgroundColor: 'primary.main', 
                    borderRadius: '50%' 
                  }} 
                />
                {selectedProducts.size} productos seleccionados
              </Typography>
            )}
          </Box>
          <Box display="flex" gap={1}>
            <Button onClick={handleClose} variant="outlined">
              Cerrar
            </Button>
            {selectedProducts.size > 0 && onCreatePurchaseOrder && (
              <Button
                onClick={handleCreatePurchaseOrder}
                variant="contained"
                color="success"
              >
                Crear Orden de Compra ({selectedProducts.size})
              </Button>
            )}
          </Box>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default ModalPredicciones;