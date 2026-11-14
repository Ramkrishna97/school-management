import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { InventoryItem } from './api'

export default function InventoryChart({ items }: { items: InventoryItem[] }) {
  return <div className="h-64"><ResponsiveContainer width="100%" height="100%">
    <BarChart data={items} margin={{ bottom: 20 }}>
      <CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="sku" angle={-20} textAnchor="end" height={60} />
      <YAxis /><Tooltip /><Legend /><Bar dataKey="quantity" fill="#1d4ed8" name="Stock" />
      <Bar dataKey="minimumQuantity" fill="#f97316" name="Minimum" />
    </BarChart>
  </ResponsiveContainer></div>
}
